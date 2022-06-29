package org.pepfar.pdma.app.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.pepfar.pdma.app.data.dto.PreferencesDto;
import org.pepfar.pdma.app.data.service.PreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class PropsUtil
{

	/**
	 * Properties that can be changed dynamically
	 */
	private Properties runtimeProps = new Properties();

	@Autowired
	private PreferencesService confService;

	public PropsUtil() {
		try {
			InputStream runtimeInputStream = PropsUtil.class.getClassLoader().getResourceAsStream("runtime.properties");

			runtimeProps.load(runtimeInputStream);

//			System.out.println(runtimeProps.getProperty("status.completed2closed.duration"));

		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}

	/**
	 * 
	 * Get a single-value property
	 * 
	 * @param key
	 * @return String
	 */
	public String getProperty(String key) {

		if (runtimeProps.containsKey(key)) {
			PreferencesDto conf = confService.findByName(key);

			if (conf != null) {
				return conf.getValue();
			} else {

				String value = runtimeProps.getProperty(key);

				conf = new PreferencesDto();
				conf.setName(key);
				conf.setValue(value);
				confService.saveOne(conf);

				return value;
			}
		}

		return null;
	}

	/**
	 * Get a property with default value passed
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getProperty(String key, String defaultValue) {

		String value = getProperty(key);

		return (value == null) ? defaultValue : value;

	}

	/**
	 * Get an integer property value
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public int getProperty(String key, int defaultValue) {

		int value = defaultValue;

		try {
			value = Integer.parseInt(getProperty(key));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;
	}

	/**
	 * Get a long property value
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public long getProperty(String key, long defaultValue) {

		long value = defaultValue;

		try {
			value = Long.parseLong(getProperty(key));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;
	}

	/**
	 * Get a double property value
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public double getProperty(String key, double defaultValue) {

		double value = defaultValue;

		try {
			value = Double.parseDouble(getProperty(key));
		} catch (Exception e) {
		}

		return value;
	}

	/**
	 * Get a boolean property value
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public Boolean getProperty(String key, Boolean defaultValue) {

		Boolean value = defaultValue;

		try {
			value = Boolean.valueOf(getProperty(key));
		} catch (Exception e) {
		}

		return value;
	}

	/**
	 * Update a property value
	 * 
	 * @param key
	 * @param addIfMissing
	 */
	public void updateProperty(String key, String value, Boolean addIfMissing) {

		if (runtimeProps.containsKey(key)) {
			runtimeProps.setProperty(key, value);
		} else if (addIfMissing) {
			runtimeProps.put(key, value);
		}

		// Update the database
		PreferencesDto conf = confService.findByName(key);

		if (conf != null) {
			conf.setValue(value);
			confService.saveOne(conf);
		} else {
			conf = new PreferencesDto();
			conf.setName(key);
			conf.setValue(value);
			confService.saveOne(conf);
		}
	}

	/**
	 * 
	 * Get a multi-value property
	 * 
	 * @param key
	 * @return Array of String
	 */
	public String[] getProperties(String key) {

		String value = null;

		if (runtimeProps.containsKey(key)) {
			value = runtimeProps.getProperty(key);
		}

		if (value != null) {
			if (value.indexOf(",") > 0) {
				return value.split(",");
			} else {
				return new String[] { value };
			}
		}

		return null;
	}
}
