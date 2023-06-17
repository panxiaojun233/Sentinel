package com.alibaba.csp.sentinel.core.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.csp.sentinel.core.constant.ConfigEnvConstant;
import com.alibaba.csp.sentinel.core.constant.IstioConstants;
import com.alibaba.csp.sentinel.core.util.common.io.FileUtils;
import com.alibaba.csp.sentinel.core.util.common.lang.StringUtils;


/**
 * 配置文件
 */
public class XdsConfigProperties {
    private static final Logger log = LoggerFactory.getLogger(XdsConfigProperties.class);
    private volatile static XdsConfigProperties instance = null;
    //是否开启https的额外端口
    private boolean openHttps = true;
    //客户端是否使用https
    private boolean clientUseHttps = true;
    //istio的host
    private String host;
    //istio的port
    private int port;
    //额外的端口号
    private Integer httpsPort = 8537;
    /**
     * jwt token for istiod 15012 port.
     */
    private String istiodToken;
    //是否打印日志
    private Boolean logXds;
    private Boolean useAgent;
    private String podName;
    private String caAddr;
    private String jwtPolicy;
    private String trustDomain;
    //必须填对
    private String namespaceName;
    private String eccSigAlg;
    //证书时间,单位S
    private int secretTTL;
    private int rsaKeySize;
    //轮转周期
    private float secretGracePeriodRatio;
    private String istioMetaClusterId;
    private String serviceAccountName;
    private String applicationName;
    private XdsConfigProperties() {

    }

    public static XdsConfigProperties getInstance() {
        if (instance == null) {
            synchronized (XdsConfigProperties.class) {
                if (instance == null) {
                    instance = getXdsConfigFromEnv();
                }
            }
        }
        return instance;
    }

    private static XdsConfigProperties getXdsConfigFromEnv() {
        XdsConfigProperties xdsConfigProperties = new XdsConfigProperties();
        xdsConfigProperties.setHost(getEnvOrElse(ConfigEnvConstant.istio_host_env, ConfigEnvConstant.istio_host_def));
        xdsConfigProperties.setPort(Integer.parseInt(getEnvOrElse(ConfigEnvConstant.istio_port_env, ConfigEnvConstant.istio_port_def)));
        xdsConfigProperties.setServiceAccountName(getEnvOrElse(ConfigEnvConstant.service_account_name_env, ConfigEnvConstant.service_account_name_def));
        xdsConfigProperties.setUseAgent(Boolean.parseBoolean(getEnvOrElse(ConfigEnvConstant.use_agent_env, ConfigEnvConstant.use_agent_def)));
        xdsConfigProperties.setIstiodToken(getEnvOrElse(ConfigEnvConstant.istiod_token_env, ConfigEnvConstant.istiod_token_def));
        xdsConfigProperties.setNamespaceName(getEnvOrElse(ConfigEnvConstant.tls_namespace_env, ConfigEnvConstant.tls_namespace_def));
        xdsConfigProperties.setApplicationName(getEnvOrElse(ConfigEnvConstant.tls_application_name_env, ConfigEnvConstant.tls_application_name_def));
        xdsConfigProperties.init();
        return xdsConfigProperties;
    }

    private static String getToken() {
        File namespaceFile = new File(ConfigEnvConstant.tokenPATH);
        if (namespaceFile.canRead()) {
            try {
                return FileUtils.readFileToString(namespaceFile, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("Read istio token file error", e);
            }
        }
        return null;
    }

    private static String getEnvOrElse(String envKey, String defaultValue) {
        String envValue = System.getenv(envKey);
        return envValue != null ? envValue : defaultValue;
    }

    public boolean isOpenHttps() {
        return openHttps;
    }


    public void setOpenHttps(boolean openHttps) {
        this.openHttps = openHttps;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
    }

    public boolean isClientUseHttps() {
        return clientUseHttps;
    }

    public void setClientUseHttps(boolean clientUseHttps) {
        this.clientUseHttps = clientUseHttps;
    }

    public Boolean getLogXds() {
        return logXds;
    }

    public void init() {
        if (this.port <= 0 || this.port > 65535) {
            this.port = IstioConstants.ISTIOD_SECURE_PORT;
        }
        if (StringUtils.isEmpty(host)) {
            this.host = IstioConstants.DEFAULT_ISTIOD_ADDR;
        }
        if (logXds == null) {
            logXds = true;
        }
        if (useAgent == null) {
            useAgent = false;
        }
        if (podName == null) {
            podName = getEnvOrElse(IstioConstants.POD_NAME, IstioConstants.DEFAULT_POD_NAME);
        }
        if (caAddr == null) {
            caAddr = getEnvOrElse(IstioConstants.CA_ADDR_KEY, this.host + ":" + this.port);
        }
        if (jwtPolicy == null) {
            jwtPolicy = IstioConstants.FIRST_PARTY_JWT;
        }
        if (trustDomain == null) {
            trustDomain = IstioConstants.DEFAULT_TRUST_DOMAIN;
        }

        if (eccSigAlg == null) {
            eccSigAlg = getEnvOrElse(IstioConstants.ECC_SIG_ALG_KEY, IstioConstants.DEFAULT_ECC_SIG_ALG);
        }
        if (secretGracePeriodRatio <= 0) {
            secretGracePeriodRatio = Float.parseFloat(getEnvOrElse(IstioConstants.SECRET_GRACE_PERIOD_RATIO_KEY, IstioConstants.DEFAULT_SECRET_GRACE_PERIOD_RATIO));
        }
        if (secretTTL <= 0) {
            secretTTL = Integer.parseInt(getEnvOrElse(IstioConstants.SECRET_TTL_KEY, IstioConstants.DEFAULT_SECRET_TTL));
        }
        if (istioMetaClusterId == null) {
            istioMetaClusterId = IstioConstants.DEFAULT_ISTIO_META_CLUSTER_ID;
        }
        if (rsaKeySize <= 0) {
            rsaKeySize = Integer.parseInt(getEnvOrElse(IstioConstants.RSA_KEY_SIZE_KEY, IstioConstants.DEFAULT_RSA_KEY_SIZE));
        }
    }


    private String getNamespaceFromFile() {
        File namespaceFile = new File(IstioConstants.KUBERNETES_NAMESPACE_PATH);
        if (namespaceFile.canRead()) {
            try {
                return FileUtils.readFileToString(namespaceFile, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("Read k8s namespace file error", e);
            }
        }
        return null;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIstiodToken() {
        if (this.istiodToken != null) {
            return this.istiodToken;
        } else {
            return getToken();
        }
    }

    public void setIstiodToken(String istiodToken) {
        this.istiodToken = istiodToken;
    }

    public boolean isLogXds() {
        return Boolean.TRUE.equals(logXds);
    }

    public void setLogXds(boolean logXds) {
        this.logXds = logXds;
    }

    public void setLogXds(Boolean logXds) {
        this.logXds = logXds;
    }

    public Boolean getUseAgent() {
        return useAgent;
    }

    public void setUseAgent(Boolean useAgent) {
        this.useAgent = useAgent;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getCaAddr() {
        return caAddr;
    }

    public void setCaAddr(String caAddr) {
        this.caAddr = caAddr;
    }

    public String getJwtPolicy() {
        return jwtPolicy;
    }

    public void setJwtPolicy(String jwtPolicy) {
        this.jwtPolicy = jwtPolicy;
    }

    public String getTrustDomain() {
        return trustDomain;
    }

    public void setTrustDomain(String trustDomain) {
        this.trustDomain = trustDomain;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public float getSecretGracePeriodRatio() {
        return secretGracePeriodRatio;
    }

    public void setSecretGracePeriodRatio(float secretGracePeriodRatio) {
        this.secretGracePeriodRatio = secretGracePeriodRatio;
    }

    public int getSecretTTL() {
        return secretTTL;
    }

    public void setSecretTTL(int secretTTL) {
        this.secretTTL = secretTTL;
    }

    public String getEccSigAlg() {
        return eccSigAlg;
    }

    public void setEccSigAlg(String eccSigAlg) {
        this.eccSigAlg = eccSigAlg;
    }

    public String getIstioMetaClusterId() {
        return istioMetaClusterId;
    }

    public void setIstioMetaClusterId(String istioMetaClusterId) {
        this.istioMetaClusterId = istioMetaClusterId;
    }

    public int getRsaKeySize() {
        return rsaKeySize;
    }

    public void setRsaKeySize(int rsaKeySize) {
        this.rsaKeySize = rsaKeySize;
    }

    public String getServiceAccountName() {
        return serviceAccountName;
    }

    public void setServiceAccountName(String serviceAccountName) {
        this.serviceAccountName = serviceAccountName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

}
