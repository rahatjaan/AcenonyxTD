package com.integration.test;

public class Test {
	public static void main(String[] args){
		String value = "<root>   <order_number>123</order_number>   <parameters>      <commentText>sdfgdfgdf sfgdgf</commentText>      <emailAddress>madank@chetu.com</emailAddress>      <numberOfPeople>122</numberOfPeople>      <patronName>sdf</patronName>      <phoneNumber>3455435</phoneNumber>      <tableNumber>1590</tableNumber>      <tenantId>3</tenantId>      <terminalId>touchdine-2</terminalId>   </parameters>   <products>      <element>         <id>88</id>         <modifiers>            <element>               <groupid>1</groupid>               <modifierslist>                  <element>1</element>                  <element>4</element>               </modifierslist>            </element>            <element>               <groupid>2</groupid>               <modifierslist>                  <element>2</element>                  <element>5</element>               </modifierslist>            </element>         </modifiers>         <quantity>2</quantity>         <size_id>1</size_id>      </element>      <element>         <id>90</id>         <modifiers>            <element>               <groupid>1</groupid>               <modifierslist>                  <element>1</element>                  <element>4</element>               </modifierslist>            </element>            <element>               <groupid>2</groupid>               <modifierslist>                  <element>2</element>                  <element>5</element>               </modifierslist>            </element>         </modifiers>         <quantity>2</quantity>         <size_id>1</size_id>      </element>   </products></root>";
		
		int ind1 = value.indexOf("<products>");
		int ind2 = value.indexOf("</products>");
		String val = value.substring(ind1,ind2);
		while(ind1 < ind2 && val.contains("<id>")){
			ind1 = val.indexOf("<id>");
			System.out.println(val.substring(ind1+4,val.indexOf("</id>")));
			ind1 = val.indexOf("<quantity>");
			System.out.println(val.substring(ind1+10,val.indexOf("</quantity>")));
			val = val.substring(val.indexOf("</quantity>")+11);
			System.out.println(val);
		}		
		
	}
}
