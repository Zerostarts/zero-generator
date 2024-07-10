package com.star.web.manager;

import cn.hutool.core.collection.CollUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.exception.MultiObjectDeleteException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.transfer.Download;
import com.qcloud.cos.transfer.TransferManager;
import com.star.web.config.CosClientConfig;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

/**
 * Cos 对象存储操作
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    //复用下载对象
    private TransferManager transferManager;

    @PostConstruct
    public void init() {
        System.out.println("Bean 初始化成功");
        // 多线程并发上传下载
        // 自定义线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
        // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
        ExecutorService threadPool = Executors.newFixedThreadPool(32);
        transferManager = new TransferManager(cosClient, threadPool);
    }

    /**
     * 上传对象
     *
     * @param key           唯一键
     * @param localFilePath 本地文件路径
     * @return
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                new File(localFilePath));
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }


    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key
     * @param localFilePath
     * @return
     * @throws InterruptedException
     */
    public Download download(String key, String localFilePath) throws InterruptedException {
        File downloadFile = new File(localFilePath);
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        Download download = transferManager.download(getObjectRequest, downloadFile);
        download.waitForCompletion();
        return download;
    }


    /**
     * 流式处理代码
     *
     * @param cosObjectInput
     * @param out
     * @throws IOException
     */
    public void download2Stream(COSObjectInputStream cosObjectInput, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = cosObjectInput.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }

    /**
     * 删除对象
     *
     * @param key
     * @throws CosClientException
     * @throws CosServiceException
     */
    public void deleteObject(String key) throws CosClientException, CosServiceException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }


    /**
     * 批量删除对象
     * 批量删除一定不能以 / 开头
     */
    public DeleteObjectsResult deleteObjects(List<String> keyList) throws MultiObjectDeleteException, CosClientException, CosServiceException {
        if (CollUtil.isEmpty(keyList)) {
            return null;
        }
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosClientConfig.getBucket());
        // 设置要删除的key列表, 最多一次删除1000个

        ArrayList<DeleteObjectsRequest.KeyVersion> keyVersions = new ArrayList<>();
        // 传入要删除的文件名
        // 注意文件名不允许以正斜线/或者反斜线\开头，例如：
        // 存储桶目录下有a/b/c.txt文件，如果要删除，只能是 keyList.add(new KeyVersion("a/b/c.txt")), 若使用 keyList.add(new KeyVersion("/a/b/c.txt"))会导致删除不成功

        for (String key : keyList) {
            keyVersions.add(new DeleteObjectsRequest.KeyVersion(key));
        }

        deleteObjectsRequest.setKeys(keyVersions);
        DeleteObjectsResult deleteObjectsResult = cosClient.deleteObjects(deleteObjectsRequest);
        return deleteObjectsResult;
    }

    /**
     * 删除目录
     *
     * @param delPrefix 目录前缀，不是精确得到目录，尽量以/结尾，防止误删
     * @throws CosClientException
     * @throws CosServiceException
     */
    public void deleteDir(String delPrefix) throws CosClientException, CosServiceException {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        // 设置 bucket 名称
        listObjectsRequest.setBucketName(cosClientConfig.getBucket());
        // prefix 表示列出的对象名以 prefix 为前缀
        // 这里填要列出的目录的相对 bucket 的路径
        listObjectsRequest.setPrefix(delPrefix);
        listObjectsRequest.setMaxKeys(1000);
        // 保存每次列出的结果
        ObjectListing objectListing = null;

        do {
            objectListing = cosClient.listObjects(listObjectsRequest);

            // 这里保存列出的对象列表
            List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries();
            ArrayList<DeleteObjectsRequest.KeyVersion> delObjects = new ArrayList<DeleteObjectsRequest.KeyVersion>();
            for (COSObjectSummary cosObjectSummary : cosObjectSummaries) {
                delObjects.add(new DeleteObjectsRequest.KeyVersion(cosObjectSummary.getKey()));
            }
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosClientConfig.getBucket());
            deleteObjectsRequest.setKeys(delObjects);
            DeleteObjectsResult deleteObjectsResult = cosClient.deleteObjects(deleteObjectsRequest);
            List<DeleteObjectsResult.DeletedObject> deleteObjectResultArray = deleteObjectsResult.getDeletedObjects();
            // 标记下一次开始的位置
            String nextMarker = objectListing.getNextMarker();
            listObjectsRequest.setMarker(nextMarker);
        } while (objectListing.isTruncated());

    }


}
