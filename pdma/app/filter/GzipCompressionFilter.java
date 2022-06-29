package org.pepfar.pdma.app.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public class GzipCompressionFilter extends OncePerRequestFilter
{

	@Override
	protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse,
			FilterChain filterChain) throws ServletException, IOException {

		if (servletRequest instanceof HttpServletRequest) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
			HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
			if (supportsGzip(httpServletRequest)) {
				GzipCompressionResponseWrapper responseWrapper = new GzipCompressionResponseWrapper(
						httpServletResponse);
				filterChain.doFilter(servletRequest, responseWrapper);
				responseWrapper.finishResponse();
			} else {
				filterChain.doFilter(servletRequest, servletResponse);
			}
		}

	}

	private boolean supportsGzip(HttpServletRequest httpServletRequest) {
		boolean accepts = false;
		String encoding = httpServletRequest.getHeader("Accept-Encoding");
		if (null != encoding && encoding.indexOf("gzip") > -1) {
			accepts = true;
		}
		return accepts;
	}
}
