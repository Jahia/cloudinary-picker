package org.jahia.se.modules.dam.cloudinary;

import org.apache.commons.lang3.StringUtils;
import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.bin.filters.ServletWrappingFilter;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.mitre.dsmiley.httpproxy.URITemplateProxyServlet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This proxy is used to call the cloudinary admin API from the piker
 * in order to get the asset_id of the selected element
 */
@Component(service = AbstractServletFilter.class, configurationPid="org.jahia.se.modules.cloudinary_picker_credentials")
public class CloudinaryProxyServlet extends ServletWrappingFilter {

    private String authorization;

    @Activate
    public void onActivate(Map<String, String> params) {
        authorization = Base64
            .getEncoder()
            .encodeToString(
                    (params.get("cloudinary_provider.apiKey") + ":" + params.get("cloudinary_provider.apiSecret"))
                    .getBytes(StandardCharsets.UTF_8)
            );

        setServletName("cloudinary");
        setServletClass(URITemplateProxyServlet.class);

        Map<String, String> initParams = new HashMap<>();
        StringBuilder targetUri = new StringBuilder();
        targetUri.append(params.get("cloudinary_provider.apiSchema"));
        targetUri.append("://");
        targetUri.append(params.get("cloudinary_provider.apiEndPoint"));
        targetUri.append("/");
        targetUri.append(params.get("cloudinary_provider.apiVersion"));
        targetUri.append("/");
        targetUri.append(params.get("cloudinary_provider.cloudName"));
        targetUri.append("/{_path}");

        initParams.put(ProxyServlet.P_TARGET_URI, targetUri.toString());
        initParams.put(ProxyServlet.P_LOG, "true");
        setInitParameters(initParams);

        Set<String> dispatcherTypes = new HashSet<>();
        dispatcherTypes.add("REQUEST");
        dispatcherTypes.add("FORWARD");
        setDispatcherTypes(dispatcherTypes);

        setUrlPatterns(new String[]{"/cloudinary/*"});
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        super.doFilter(new HttpServletRequestWrapper((HttpServletRequest) servletRequest) {
            @Override
            public String getHeader(String name) {
                if ("Authorization".equals(name)) {
                    return "Basic " + authorization;
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if ("Authorization".equals(name)) {
                    return Collections.enumeration(Collections.singleton("Basic " + authorization));
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = Collections.list(super.getHeaderNames());
                names.add("Authorization");
                return Collections.enumeration(names);
            }

            @Override
            public String getQueryString() {
                return "_path=" + StringUtils.substringAfter(((HttpServletRequest) servletRequest).getRequestURI(), "/cloudinary/");
            }
        }, servletResponse, filterChain);
    }
}
