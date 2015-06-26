package org.openhab.binding.pcf8591control.internal;

public class AddressAndPin implements Comparable<AddressAndPin> {
	public int address;
	public int pin;
	
	public AddressAndPin(int address, int pin) {
		this.address = address;
		this.pin = pin;
	}

	@Override
	public int compareTo(AddressAndPin addressAndPin) {
		if(address == addressAndPin.address && pin == addressAndPin.pin){
			return 0;
		} else {
			return -1;			
		}
	}
}
