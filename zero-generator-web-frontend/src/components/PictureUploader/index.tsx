import {LoadingOutlined, PlusOutlined} from '@ant-design/icons';
import type {UploadProps} from 'antd';
import {message, Upload} from 'antd';
import React, {useState} from "react";
import {uploadFileUsingPost,} from "@/services/backend/fileController";
import {COS_HOST} from "@/constants";




interface Props {
  biz: string,
  onChange?:(url:string) => void,
  value? :string,

}


const PictureUploader: React.FC<Props> = (props) => {
  const [loading, setLoading] = useState(false);
  const {biz, value, onChange} = props;

  const uploadProps: UploadProps = {
    name: 'file',
    listType: 'picture-card',
    multiple: false,
    maxCount: 1,
    showUploadList:false,
    customRequest: async (fileObj: any) => {
      setLoading(true);
      try {
        const res = await uploadFileUsingPost({
          biz,
        },
        {}
        , fileObj.file);
        const fullPath = COS_HOST + res.data;
        onChange?.(fullPath??'');
        fileObj.onSuccess(res.data);
      } catch (e: any) {
        message.error('上传失败，' + e.message);
        fileObj.onError(e);
      }
      setLoading(false);
    },
  };

  const uploadButton = (
    <div>
      {loading ? <LoadingOutlined /> : <PlusOutlined />}
    </div>
  )

  return (
    <Upload {...uploadProps}>
      {value ? <img src={value} alt="picture"  style={{width: '100%'}} /> : uploadButton}
    </Upload>
  );
};

export default PictureUploader;
