# pcf8591control
Controlling the chip pcf8591 (AD-converter) via I2C from Raspberry Pi from openHAB

The chip PCF8591 is an AD-Converter and provides measurement of voltage and returns values from 0 to 255. 
This binding supports only reading values.

## Config in *.item file
In the <b>items-file</b> of openHAB the following <b>configuration</b> is needed:<br>
Number Name-of-Item { pcf8591control="I2CAddressInDecimal;PinNumber" }

**Example:**<br>
Number brightness { pcf8591control="72;0" } <br>
This would get the Pin 0 from the PCF8591 chip with the address 0x48 (72 in decimal)

## Config in *.sitemap file
**Example** to show the read value on the website:<br>
Text item=brightness label="Brightness value: [%s]"
