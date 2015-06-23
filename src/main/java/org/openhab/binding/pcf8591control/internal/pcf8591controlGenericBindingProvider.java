/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pcf8591control.internal;

import java.io.IOException;
import java.util.TreeMap;

import org.openhab.binding.pcf8591control.pcf8591controlBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;


/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author MichaelP
 * @since 1.0
 */
public class pcf8591controlGenericBindingProvider extends AbstractGenericBindingProvider implements pcf8591controlBindingProvider {

	private static final Logger logger = 
			LoggerFactory.getLogger(pcf8591controlGenericBindingProvider.class);
	
	I2CBus bus;
	private TreeMap<Integer, I2CDevice> PCF8591Map = new TreeMap<>();
	
	public pcf8591controlGenericBindingProvider() {
		 try {
			bus = I2CFactory.getInstance(I2CBus.BUS_1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBindingType() {
		return "pcf8591control";
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
		if (!(item instanceof SwitchItem  || item instanceof ContactItem || item instanceof NumberItem)) {
			throw new BindingConfigParseException("item '" + item.getName()
					+ "' is of type '" + item.getClass().getSimpleName()
					+ "', only Switch- and DimmerItems and ContactItems are allowed - please check your *.items configuration");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {		
		super.processBindingConfiguration(context, item, bindingConfig);
			
		//Format: "I2CAddress;PinNumber;isOutput" => f.e. "32;0;out" or "32;1;in"
		String[] properties = bindingConfig.split(";");		
		pcf8591controlConfig config = new pcf8591controlConfig();
		try{
			
			config.address = Integer.parseInt(properties[0]);
			config.pinNumber = Integer.parseInt(properties[1]);
			
			checkOfValidValues(config, item.getName());
			addBindingConfig(item, config);	
			handleBoards(config); //Create new PCF8591GpioProvider for eventually new boards.
				
			logger.debug("processBindingConfiguration: (pcf8591control) ItemName: {}, Addresses: {}", item.toString(), PCF8591Map.keySet());
			
		}catch(Exception e){
			e.printStackTrace();
		}
		logger.debug("pcf8591controlGenericBindingProvider: processBindingConfiguration({},{}) is called!", config.address, config.pinNumber);
		//parse bindingconfig here ...
				
	}
	
	/* ================================= SELF WRITTEN METHODS - BEGIN ===============================*/
	
	private void checkOfValidValues(pcf8591controlConfig config, String itemName){
		if(config.address < 72 || config.address > 80 ){
			throw new IllegalArgumentException("The given address '" + config.address + "'of the item '" + itemName + "' is invalid! " +
					"PCA8591 must be between 72-80 (0x48-0x50)");
		}
		
		if(config.pinNumber < 0 || config.pinNumber > 3){
			throw new IllegalArgumentException("The pinNumber of the item '" + itemName + "'is invalid! Must be between 0-3.");
		}				
	}
		
	private void handleBoards(pcf8591controlConfig config){
		try {
			if(!PCF8591Map.containsKey(config.address)){
				try{
					I2CDevice device = bus.getDevice(config.address);
					PCF8591Map.put(config.address, device);	
					logger.debug("handleBoards: added PCF8591 board with address: {} !", config.address);
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
			removeUnusedBoardsFromMap(config);
		} catch (Exception e) {
			e.printStackTrace();
			//logger.debug("Exception in PCF8591 handleBoards... however, it works.");
		}
	}
	
	private void removeUnusedBoardsFromMap(pcf8591controlConfig config){
		keyLoop:
		for(Integer mapKey : PCF8591Map.keySet()){
			logger.debug("handleBoards: mapKey {} !", mapKey);
			for(BindingConfig bindingConfig : bindingConfigs.values()){
				pcf8591controlConfig conf = (pcf8591controlConfig) bindingConfig;				
				logger.debug("handleBoards: check {} !", conf.address);
				if(mapKey == conf.address){
					logger.debug("handleBoards: board found with address: {} !", conf.address);
					continue keyLoop;
				}				
			}
			if(!bindingConfigs.values().isEmpty()){
				PCF8591Map.remove(mapKey);
				logger.debug("handleBoards: removed board with address: {} !", mapKey);
			}
		}
	}
	
	
	@Override
	public int getAddress(String itemName) {
		pcf8591controlConfig config = (pcf8591controlConfig) bindingConfigs.get(itemName);
		
		if (config == null) {
			throw new IllegalArgumentException("The item name '" + itemName + "'is invalid or the item isn't configured");
		}
		
		return config.address;
	}

	@Override
	public int getPinNumber(String itemName) {
		pcf8591controlConfig config = (pcf8591controlConfig) bindingConfigs.get(itemName);
		
		if (config == null) {
			throw new IllegalArgumentException("The item name '" + itemName + "'is invalid or the item isn't configured");
		}
		
		return config.pinNumber;
	}	
	
	@Override
	public boolean isItemConfigured(String itemName) {
		if (bindingConfigs.containsKey(itemName)) {
			return true;
		}
		return false;
	}
	
	
	public class pcf8591controlConfig implements BindingConfig{
		int address;
		int pinNumber;
	}


	@Override
	public TreeMap<Integer, I2CDevice> getPCF8591Map() {		
		return PCF8591Map;
	}

	
	/* ================================= SELF WRITTEN METHODS - END ===============================*/
	
}
