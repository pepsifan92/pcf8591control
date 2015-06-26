# pcf8591control
Controlling the chip pcf8591 (AD-converter) via I2C from Raspberry Pi from openHAB

The chip PCF8591 is an AD-Converter and provides measurement of voltage and returns values from 0 to 255. <br>
**Addressrange of PCF8591:** 72-80 (0x48-0x50)<br>
This binding supports only reading values.

## Config in *.item file
In the **items-file** of openHAB the following **configuration** is needed:<br>
`Number Name-of-Item { pcf8591control="I2CAddressInDecimal;PinNumber" }`

**Example:**<br>
`Number brightness { pcf8591control="72;0" }` <br>
This would get the Pin 0 from the PCF8591 chip with the address 0x48 (72 in decimal)

## Config in *.sitemap file
**Example** to show the read value on the website:<br>
`Text item=brightness label="Brightness value: [%s]"`
