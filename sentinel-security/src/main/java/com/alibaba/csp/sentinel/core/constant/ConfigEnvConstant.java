package com.alibaba.csp.sentinel.core.constant;

public class ConfigEnvConstant {

	public static String istio_host_env = "ISTIO_HOST";

	public static String istio_host_def = "istiod.istio-system.svc";


	public static String istio_port_env = "ISTIO_PORT";


	public static String istio_port_def = "15012";


	public static String service_account_name_env = "KUBERNETES_POD_SERVICE_ACCOUNT";

	public static String service_account_name_def = null;


	public static String use_agent_env = "USE_AGENT";

	public static String use_agent_def = "true";


	public static String istiod_token_env = "ISTIO_TOKEN";

	public static String tokenPATH = "/var/run/secrets/tokens/mse-token";

	public static String istiod_token_def = null;


	public static String tls_namespace_env = "KUBERNETES_POD_NAMESPACE";

	public static String tls_namespace_def = null;


	public static String tls_application_name_env = "TLS_APPLICATION_NAME";

	public static String tls_application_name_def = null;

}
