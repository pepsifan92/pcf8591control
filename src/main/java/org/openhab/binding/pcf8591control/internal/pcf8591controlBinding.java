/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pcf8591control.internal;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.pcf8591control.pcf8591controlBindingProvider;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CDevice;

/**
 * Implement this class if you are going create an actively polling service
 * like querying a Website/Device.
 * 
 * @author MichaelP
 * @since 1.0
 */
public class pcf8591controlBinding extends AbstractActiveBinding<pcf8591controlBindingProvider> {

	private static final Logger logger = 
		LoggerFactory.getLogger(pcf8591controlBinding.class);	
		
	/**
	 * The BundleContext. This is only valid when the bundle is ACTIVE. It is set in the activate()
	 * method and must not be accessed anymore once the deactivate() method was called or before activate()
	 * was called.
	 */
	private BundleContext bundleContext;
	private TreeMap<AddressAndPin, Integer> PinStateMap = new TreeMap<>();
	
	/** 
	 * the refresh interval which is used to poll values from the pcf8591control
	 * server (optional, defaults to 5000ms)
	 */
	private long refreshInterval = 5000;
	
	public pcf8591controlBinding() {
		logger.debug("pcf8591controlBinding binding started");
	}
			
	
	/* ADDED THIS FOR GETTING ALL COMMANDS... ****************************/
	/**
	 * @{inheritDoc}
	 */
	@Override
	public void receiveCommand(String itemName, Command command) {
		// does any provider contain a binding config?
		if (!providesBindingFor(itemName)) {
			return;
		}
		internalReceiveCommand(itemName, command);
	}
	/* ADDED THIS FOR GETTING ALL COMMANDS... ****************************/
	
	
	/**
	 * Called by the SCR to activate the component with its configuration read from CAS
	 * 
	 * @param bundleContext BundleContext of the Bundle that defines this component
	 * @param configuration Configuration properties for this component obtained from the ConfigAdmin service
	 */
	public void activate(final BundleContext bundleContext, final Map<String, Object> configuration) {
		this.bundleContext = bundleContext;			
		// to override the default refresh interval one has to add a 
		// parameter to openhab.cfg like <bindingName>:refresh=<intervalInMs>
		String refreshIntervalString = (String) configuration.get("refresh");
		if (StringUtils.isNotBlank(refreshIntervalString)) {
			refreshInterval = Long.parseLong(refreshIntervalString);
		}
		
		setProperlyConfigured(true);
	}
	
	/**
	 * Called by the SCR when the configuration of a binding has been changed through the ConfigAdmin service.
	 * @param configuration Updated configuration properties
	 */
	public void modified(final Map<String, Object> configuration) {
		// update the internal configuration accordingly
	}
	
	/**
	 * Called by the SCR to deactivate the component when either the configuration is removed or
	 * mandatory references are no longer satisfied or the component has simply been stopped.
	 * @param reason Reason code for the deactivation:<br>
	 * <ul>
	 * <li> 0 – Unspecified
     * <li> 1 – The component was disabled
     * <li> 2 – A reference became unsatisfied
     * <li> 3 – A configuration was changed
     * <li> 4 – A configuration was deleted
     * <li> 5 – The component was disposed
     * <li> 6 – The bundle was stopped
     * </ul>
	 */
	public void deactivate(final int reason) {
		this.bundleContext = null;
		// deallocate resources here that are no longer needed and 
		// should be reset when activating this binding again		
	}

	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected String getName() {
		return "pcf8591control Refresh Service";
	}
	

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void execute() {
		// the frequently executed code (polling) goes here ...		
		readAllInputPins();
	}
		
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		// the code being executed when a command was sent on the openHAB
		// event bus goes here. This method is only called if one of the
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("pcf8591control: internalReceiveCommand({},{}) is called!", itemName, command);
		
		//There is no output, just reading. Reading happens automatically through execute... 
	}
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveUpdate(String itemName, State newState) {
		// the code being executed when a state was sent on the openHAB
		// event bus goes here. This method is only called if one of the 
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("internalReceiveUpdate({},{}) is called!", itemName, newState);		
	}	
	
	private void readAllInputPins(){
		//logger.debug("<<<<<<<<<<<<<<<<<<<<<< READ ALL INPUT PINS is called! >>>>>>>>>>>>>>>>>>>>>>>>>");
		for (pcf8591controlBindingProvider provider : providers) {			
			for(Entry<Integer, I2CDevice> entry : provider.getPCF8591Map().entrySet()){ //Every Board				
				int key = entry.getKey();
				I2CDevice prov = entry.getValue();	
				int TempPinReadVal = 0;
				for(int pin = 0; pin <= 4; pin++){
					try {
						AddressAndPin addressAndPin = new AddressAndPin(key, pin);
						if(PinStateMap.containsKey(addressAndPin)){						
						
							prov.read(pin); //Read fist time to get the second read, because the first value is old...
							TempPinReadVal = prov.read(pin);
							if (PinStateMap.get(addressAndPin).intValue() != TempPinReadVal) {
								//only if value has changed
								eventPublisher.postUpdate(getItemName(key, pin),DecimalType.valueOf(String.valueOf(TempPinReadVal)));
							}
							PinStateMap.replace(addressAndPin, TempPinReadVal);
						} else {						 
							PinStateMap.put(new AddressAndPin(key, pin), TempPinReadVal);
						}
					} catch (Exception e) {
						//Exception occurs, if Pin is not given in ItemConfig because getItemName can't find the Item ... So simply ignore it.						
					}					
				}				
			}
		}		
	}
	
	private String getItemName(int address, int pin){
		for (pcf8591controlBindingProvider provider : providers) {
			for (String itemName : provider.getItemNames()) {
				if(provider.getAddress(itemName) == address && provider.getPinNumber(itemName) == pin){
					return itemName;
				}
			}		
		}
		return "ItemNotFound in getItemName in pcf8591control";
	}
}
