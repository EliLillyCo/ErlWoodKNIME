package org.erlwood.knime.utils;

/**
 * Adapter to IWebServicePreferenceSupplier when only the getURLPreferenceId needs overriding.
 *
 * @author Tom Wilkin
 */
public abstract class AbstractWebServicePreferenceSupplierAdapter implements IWebServicePreferenceSupplier {

	@Override
	public String getURLSuffix() {
		// null implementation for adapter
		return null;
	}

	@Override
	public String getKeyPrefix() {
		// null implementation for adapter
		return null;
	}

}
