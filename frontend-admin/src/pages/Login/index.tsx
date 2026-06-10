import { LoginFormPage, ProFormText } from '@ant-design/pro-components';
import { history, useLocation, useModel } from '@umijs/max';
import { Alert, message } from 'antd';
import { useState } from 'react';
import { HOME_PATH } from '@/constants/auth';
import { login } from '@/services/user/api';
import { tokenStore } from '@/utils/token';

/**
 * 后台登录页。
 * 第一版复用用户域登录接口，先收口后台演示入口，不额外引入管理员专属认证体系。
 */
const LoginPage = () => {
  const location = useLocation();
  const { setInitialState } = useModel('@@initialState');
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  /**
   * 提交登录表单。
   * 登录成功后统一写入 tokenStore，并按 redirect 或默认首页跳转。
   *
   * @param values 登录表单值
   * @returns 表单提交流程是否结束
   */
  const handleSubmit = async (values: { username: string; password: string }) => {
    setSubmitting(true);
    setErrorMessage('');

    try {
      const response = await login(values);
      tokenStore.setSession({
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
        username: response.username,
        displayName: response.displayName,
      });
      await setInitialState?.({
        isLogin: true,
        displayName: response.displayName || response.username,
      });
      message.success('登录成功');

      const redirect = new URLSearchParams(location.search).get('redirect');
      history.replace(redirect || HOME_PATH);
      return true;
    } catch (error) {
      const messageText =
        error instanceof Error ? error.message : '登录失败，请检查用户名和密码';
      setErrorMessage(messageText);
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <LoginFormPage
      title="高并发购票系统后台"
      subTitle="聚焦活动、订单、支付对账异常与观测入口的治理控制台"
      backgroundImageUrl={undefined}
      onFinish={handleSubmit}
      submitter={{
        searchConfig: {
          submitText: '登录后台',
        },
        submitButtonProps: {
          loading: submitting,
        },
      }}
    >
      {errorMessage ? <Alert type="error" showIcon message={errorMessage} /> : null}
      <ProFormText
        name="username"
        label="用户名"
        placeholder="请输入用户名"
        rules={[{ required: true, message: '请输入用户名' }]}
      />
      <ProFormText.Password
        name="password"
        label="密码"
        placeholder="请输入密码"
        rules={[{ required: true, message: '请输入密码' }]}
      />
    </LoginFormPage>
  );
};

export default LoginPage;
