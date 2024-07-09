import FileUploader from '@/components/FileUploader';
import PictureUploader from '@/components/PictureUploader';

import {useSearchParams} from '@@/exports';
import type {ProFormInstance} from '@ant-design/pro-components';
import {
  ProCard, ProForm,
  ProFormSelect,
  ProFormText,
  ProFormTextArea, StepsForm,
} from '@ant-design/pro-components';
import {ProFormItem} from '@ant-design/pro-form';

import React, {useEffect, useRef, useState} from 'react';
import {
  addGeneratorUsingPost,
  editGeneratorUsingPost,
  getGeneratorVoByIdUsingGet, makeGeneratorUsingPost
} from "@/services/backend/generatorController";
import {COS_HOST} from "@/constants";
import {history} from '@umijs/max';
import {Collapse, Form, message, UploadFile} from "antd";
import ModelConfigForm from "@/pages/Generator/Add/components/ModelConfigForm";
import FileConfigForm from "@/pages/Generator/Add/components/FileConfigForm";
import {saveAs} from "file-saver";


interface Props {
  meta: API.GeneratorAddRequest | API.GeneratorEditRequest;
}

export default (props: Props) => {
  const {meta} = props;
  const [form] = Form.useForm();


  const doSubmit = async (values: API.GeneratorMakeRequest) => {
    //校验
    if (!meta.name) {
      message.error('请填写名称');
      return;
    }
    const zipFilePath = values.zipFilePath;
    if (!zipFilePath || zipFilePath.length < 1) {
      message.error('请上传模板文件压缩包');
      return;
    }
    // 文件列表转url
    //@ts-ignore
    values.zipFilePath = zipFilePath[0].response;

    try {
      const blob = await makeGeneratorUsingPost(
        {
          meta,
          zipFilePath: values.zipFilePath,
        },
        {
          responseType: 'blob'
        },
      )
      saveAs(blob, meta.name + '.zip');
    } catch (error: any) {
      message.error('下载失败,' + error.message);
    }
  }

  const formView = (
    <ProForm
      form={form}
      submitter={{
        searchConfig: {
          submitText: '制作',
        },
        resetButtonProps: {
          hidden: true,
        }
      }}
      onFinish={doSubmit}
    >
      <ProFormItem label="模板文件" name="zipFilePath">
        <FileUploader
          biz="generator_make_template"
          description="请上传压缩包，打包时不要添加最外层目录！"
        />
      </ProFormItem>
    </ProForm>
  )


  return (
    <Collapse style={{
      marginBottom: 24
    }}
    items={[
      {
        key: 'maker',
        label: '生成器制作工具',
        children: formView
      }
    ]}
    />
  )
}


