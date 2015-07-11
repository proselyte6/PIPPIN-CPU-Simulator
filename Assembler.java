package pippin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class Assembler {
	/**
	 * HALT, NOT, NOP
	 */
	 public static Set<String> noArgument = new TreeSet<String>();
	 /**
	 * LOD, ADD, SUB, MUL, DIV, AND, JUMP, JMPZ,
	 */
	 public static Set<String> allowsImmediate = new TreeSet<String>();
	 /**
	 * LOD, STO, ADD, SUB, MUL, DIV
	 */
	 public static Set<String> allowsIndirect = new TreeSet<String>(); 
	 
	 static {
		 noArgument.add("HALT");
		 noArgument.add("NOT");
		 noArgument.add("NOP");
		 
		 allowsImmediate.add("LOD");
		 allowsImmediate.add("ADD");
		 allowsImmediate.add("SUB");
		 allowsImmediate.add("MUL");
		 allowsImmediate.add("DIV");
		 allowsImmediate.add("AND");
		 allowsImmediate.add("JUMP");
		 allowsImmediate.add("JMPZ");

		 
		 allowsIndirect.add("LOD");
		 allowsIndirect.add("STO");
		 allowsIndirect.add("ADD");
		 allowsIndirect.add("SUB");
		 allowsIndirect.add("MUL");
		 allowsIndirect.add("DIV");
		 } 
	 
	 /**PARTNER: David Defazio
	  * Method to assemble a file to its binary representation. If the input has errors
	  * a list of errors will be written to the errors map. If there are errors,
	  * they appear as a map with the line number as the key and the description of the error
	  * as the value. If the input or output cannot be opened, the "line number" key is 0.
	  * @param input the source assembly language file
	  * @param output the binary version of the program if the souce program is
	  * correctly formatted
	  * @param errors the errors map
	  * @return
	  */
	 public static boolean assemble(File input, File output, Map<Integer, String> errors) {
		  if (errors == null){
			  throw new IllegalArgumentException("Coding error: the error map is null");
		  }
		  
		  ArrayList<String> inputText = new ArrayList<String>(); 
		  
		  try (Scanner inp = new Scanner(input)) {
			  while(inp.hasNextLine()){
				  inputText.add(inp.nextLine());
			  }
		  } catch (FileNotFoundException e) {
			  errors.put(0, "Error: Unable to open the input file");
		  }
		  
		
		  for(int i=0; i<inputText.size();i++){
			  if (inputText.get(i).trim().length() > 0){
				  if ((inputText.get(i).charAt(0) == ' ') || (inputText.get(i).charAt(0) == '\t')){
					  errors.put(i+1, "Error on line " + (i+1) + ": starts with white space"); 
				  }
			  }
		  }
		  
		  for (int i=0; i<inputText.size(); i++){
			  if (inputText.get(i).trim().length() == 0){
				  for (int k=i+1; k<inputText.size(); k++){
					  if (inputText.get(k).trim().length() > 0){
						  errors.put(i+1, "Error on line " + (i+1) + ": illegal blank line");
						  return false;
					  }
				  }
			  }
		  }
		  
		  ArrayList<String> inCode = new ArrayList<String>();
		  ArrayList<String> inData = new ArrayList<String>();
		  int dataCheck = inputText.size()-1;
		  
		  for (int i=0; i<inputText.size();i++){
			  if (inputText.get(i).trim().equalsIgnoreCase("DATA")){
			  		if (inputText.get(i).trim().equals("DATA")){
			  			dataCheck = i;
			  			break;
			  		} else {
			  			errors.put(i+1, "Error in line "+i+1+": DATA must be in caps.");
			  		}
			  }
		  }
		  
		  for (int i=0; i<dataCheck; i++){
			  inCode.add(inputText.get(i).trim());
		  }
		  
		  for (int i=dataCheck+1; i<inputText.size();i++){
			  inData.add(inputText.get(i).trim());
		  }
		  
		  ArrayList<String> outCode = new ArrayList<String>();
		  
		  for (int i=0; i<inCode.size();i++){
			  String[] parts = inCode.get(i).trim().split("\\s+");
			  
			  
			  if(!InstructionMap.opcode.containsKey(parts[0].toUpperCase())){
				  errors.put(i+1, "Error in line "+(i+1)+": Illegal mnemonic.");
				  break;
			  } else if(!InstructionMap.opcode.containsKey(parts[0])){
				  errors.put(i+1, "Error in line "+(i+1)+": Mnemonics must be uppercase");
				  break;
			  } 
			  
			  if (noArgument.contains(parts[0])){
				  if(parts.length == 1){
					  outCode.add(Integer.toString(InstructionMap.opcode.get(parts[0]),16) + " 0 0");					  
				  } else{
					  errors.put(i+1, "Error in line "+(i+1)+": Mnemonic doesn't take arguments");
				  }
			  } else if (!noArgument.contains(parts[0]) && parts.length < 2){
				  errors.put(i+1, "Error on line "+(i+1)+": No argument provided for mnemonic.");
				  break;
			  }else if (parts.length > 2){
				  errors.put(i+1, "Error in line "+(i+1)+": Mnemonic contains too many arguments");
			  } else if (parts[1].length() >= 3 && parts[1].charAt(0) == '[' && parts[1].charAt(1) == '['){
				  if (allowsIndirect.contains(parts[0])){
					  try{
						  int arg = Integer.parseInt(parts[1].substring(2,parts[1].length()),16);
						  outCode.add(Integer.toString(InstructionMap.opcode.get(parts[0].toUpperCase()),16) + " " +
						  Integer.toString(arg,16).toUpperCase() + " 2");
					  } catch(NumberFormatException e) { 
						  errors.put(i+1, "Error on line "+(i+1)+ ": indirect argument is not a hex number");
					  } 
				  } else {
					  errors.put(i+1, "Error in line "+i+1+": Mnemonic does not allow Indirect addressing");
				  }  
			  } else if (parts[1].length() >= 2 && parts[1].charAt(0) == '['){
				  try{
					  int arg = Integer.parseInt(parts[1].substring(1,parts[1].length()),16);
					  outCode.add(Integer.toString(InstructionMap.opcode.get(parts[0].toUpperCase()),16) + " " +
					  Integer.toString(arg,16).toUpperCase() + " 1"); 
				  } catch(NumberFormatException e) { 
					  errors.put(i+1, "Error on line "+(i+1)+ ": direct argument is not a hex number");
				  }
			  } else if (parts[1].length() >= 1 && parts[1].charAt(0) != '['){
				  if (allowsImmediate.contains(parts[0])){
					  try{
						  int arg = Integer.parseInt(parts[1].substring(0,parts[1].length()),16);
						  outCode.add(Integer.toString(InstructionMap.opcode.get(parts[0].toUpperCase()),16) + " " +
						  Integer.toString(arg,16).toUpperCase() + " 0");
					  } catch(NumberFormatException e) { 
						  errors.put(i+1, "Error on line "+(i+1)+ ": immediate argument is not a hex number");
					  }
				  } else {
					  errors.put(i+1, "Error on line "+(i+1)+": Mnemonic does not allow immediate addressing");
				  }
			  } 
		  }
			  	  
		  int offSet = 1+inCode.size();
		  ArrayList<String> outData = new ArrayList<String>();
		  
		  
		  for (int i = 0; i<inData.size(); i++){
			  String[] parts = inData.get(i).trim().split("\\s+");
			  if (parts.length != 2){
				  errors.put((offSet+i+1),"Error in line "+(offSet+i+1)+": This is not an address/value pair");
			  } else {
				  int addr = -1;
				  int val = -1;
				  try{
					  addr = Integer.parseInt(parts[0],16);
					  if (addr < 0){
						  errors.put((offSet+i+1),"Error in line"+(offSet+i+1)+": Address must be positive.");
						  break;
					  }
				  }catch(NumberFormatException e){
					  errors.put((offSet+i+1), "Error in line "+(offSet+i+1)+": Address is not a hex number");
				  } try {
					  val = Integer.parseInt(parts[1],16);
				  } catch (NumberFormatException e){
					  errors.put((offSet+i+1), "Error in line "+(offSet+1+i)+": Value is not a hex number");
				  }
				  outData.add(Integer.toString(addr,16).toUpperCase() + " "+ Integer.toString(val,16).toUpperCase());
			  }	  
		  }
		  if(errors.size() == 0) {
			  try (PrintWriter outp = new PrintWriter(output)){
				  for(String str : outCode) {outp.println(str);}
				  outp.println(-1); // the separator where the source has “DATA”
				  for(String str : outData) {outp.println(str);}
			  } catch (FileNotFoundException e) {
				  errors.put(0, "Error: Unable to write the assembled program to the output file");
			  }
		  }
			 return errors.size() == 0; // TRUE means there were no errors 	  
	  }
}
