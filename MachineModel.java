package pippin;

import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;
import java.util.TreeMap;
import java.util.TreeMap;

public class MachineModel extends Observable {
    public class Registers {
        private int accumulator;
        private int programCounter;
    }

    public final Map<Integer, Instruction> INSTRUCTION_MAP = new TreeMap<>();
    private Registers cpu = new Registers();
    private Memory memory = new Memory();
    private boolean withGUI = false;
    private Code code;
    private boolean running = false;
    
    public void step() {
    	int pc = cpu.programCounter;
    	int opcode = code.getOp(pc);
    	int arg = code.getArg(pc);
    	int indirectionLevel = code.getIndirectionLevel(pc);
    	get(opcode).execute(arg, indirectionLevel);   	
    }
    public void clear() {
    	memory.clear();
    	if (code != null){
    		code.clear();
    	}
    	cpu.accumulator = 0;
    	cpu.programCounter = 0;
    }
    
    public void setRunning(boolean running){
    	this.running = running;
    }
    
    public boolean isRunning(){
    	return running;
    }
    
    public void setCode(Code code){
    	this.code = code;
    }
    
    public Code getCode(){
    	return code;
    }
    
    public int getData(int index) {
        return memory.getData(index);
    }

    public void setData(int index, int value) {
        memory.setData(index, value);
    }

    public Instruction get(Integer key) {
        return INSTRUCTION_MAP.get(key);
    }

    int[] getData() {
        return memory.getData();
    }

    public int getProgramCounter() {
        return cpu.programCounter;
    }

    public int getAccumulator() {
        return cpu.accumulator;
    }

    public void setAccumulator(int i) {
        cpu.accumulator = i;
    }
    
    public void setProgramCounter(int i) {
    	 cpu.programCounter = i;
    	 }

    public int getChangedIndex() {
    	 return memory.getChangedIndex();
    }

    public void halt() {
    	 if(withGUI) {
    		 running = false;
    	 } else {
    		 System.exit(0);
    	 }
    }

    public void clearMemory() {
    	 memory.clear();
    } 

    public MachineModel(boolean withGUI) {
    	this.withGUI = withGUI;
    	
        //INSTRUCTION_MAP entry for "NOP"
    	// no operation
        INSTRUCTION_MAP.put(0x0,(arg, level) -> {
        	if (level != 0){
        		throw new IllegalArgumentException("Illegel indirection level in NOP instruction");
        	} else{
        		cpu.programCounter ++;
        	}
        });
        
        //INSTRUCTION_MAP entry for "LOD" 
        //load into accumulator from an immediate value or from memory, using direct or indirect addressing)
        INSTRUCTION_MAP.put(0x1,(arg, level) -> {
            if(level < 0 || level > 2) {
                throw new IllegalArgumentException("Illegal indirection level in LOD instruction");
            }
            if (level > 0) {
                INSTRUCTION_MAP.get(0x1).execute(memory.getData(arg), level-1); 
            } else { 
                cpu.accumulator = arg;
                cpu.programCounter ++;
            }
        });

        //INSTRUCTION_MAP entry for "STO"
        // store the accumulator into memory directly or using indirect addressing
        INSTRUCTION_MAP.put(0x2,(arg, level) -> {
            if(level < 1 || level > 2) {
                throw new IllegalArgumentException("Illegal indirection level in STO instruction");
            } if (level == 1){
            	memory.setData(arg, cpu.accumulator);
                cpu.programCounter ++;
            } else {
            	INSTRUCTION_MAP.get(0x2).execute(memory.getData(arg), level-1); 
            }
        });

        //INSTRUCTION_MAP entry for "ADD"
        // add to accumulator an immediate value or a value from memory, using direct or indirect addressing
        INSTRUCTION_MAP.put(0x3,(arg, level) -> {
            if(level < 0 || level > 2) {
                throw new IllegalArgumentException("Illegal indirection level in ADD instruction");
            }
            if (level == 0) {
            	cpu.accumulator += arg;
                cpu.programCounter ++;
            } else { 
            	INSTRUCTION_MAP.get(0x3).execute(memory.getData(arg), level-1); 
            }
        });

        //INSTRUCTION_MAP entry for "SUB"
        // subtract an immediate value or a value from memory from the accumulator, using direct or indirect addressing
        INSTRUCTION_MAP.put(0x4,(arg, level) -> {
            if(level < 0 || level > 2) {
                throw new IllegalArgumentException("Illegal indirection level in SUB instruction");
            }
            if (level == 0) {
            	cpu.accumulator -= arg;
                cpu.programCounter ++;
            } else {  
            	INSTRUCTION_MAP.get(0x4).execute(memory.getData(arg), level-1);
            }
        });

        //INSTRUCTION_MAP entry for "MUL"
        // multiply the accumulator by an immediate value or a value from memory, using direct or indirect addressing
        INSTRUCTION_MAP.put(0x5,(arg, level) -> {
            if(level < 0 || level > 2) {
                throw new IllegalArgumentException("Illegal indirection level in MUL instruction");
            }
            if (level == 0) {
            	cpu.accumulator *= arg;
                cpu.programCounter ++;
            } else { 
            	INSTRUCTION_MAP.get(0x5).execute(memory.getData(arg), level-1); 
            }
        });

        //INSTRUCTION_MAP entry for "DIV"
        //divide the accumulator by an immediate value or a value from memory, using direct or indirect addressing
        INSTRUCTION_MAP.put(0x6,(arg, level) -> {
            if(level < 0 || level > 2) {
                throw new IllegalArgumentException("Illegal indirection level in DIV instruction");
            }
            if (level == 0) {
            	if (arg == 0){
            		throw new DivideByZeroException("Division by zero");
            	} else {
            	cpu.accumulator /= arg;
            	}
                cpu.programCounter ++;
            } else {
            	INSTRUCTION_MAP.get(0x6).execute(memory.getData(arg), level-1);
            }
        });

        //INSTRUCTION_MAP entry for "AND"
        //apply Boolean “and” to accumulator and either an immediate value or a value from memory, using direct addressing
        INSTRUCTION_MAP.put(0x7,(arg, level) -> {
            if(level < 0 || level > 1) {
                throw new IllegalArgumentException("Illegal indirection level in AND instruction");
            }
            if (level == 0){
            	if(cpu.accumulator != 0 && arg != 0) {
                    cpu.accumulator = 1;            
                } else {
                    cpu.accumulator = 0;            
                }
                cpu.programCounter ++; 
            } else {
            	INSTRUCTION_MAP.get(0x7).execute(memory.getData(arg), level-1); 
            }
        });
        
        //INSTRUCTION_MAP entry for "JUMP"
        //change the program counter to an immediate value or a value from memory, using direct addressing
        INSTRUCTION_MAP.put(0xB,(arg, level) -> {
            if(level < 0 || level > 1) {
                throw new IllegalArgumentException("Illegal indirection level in JUMP instruction");
            }
            if (level == 0) {
            	cpu.programCounter = arg;
            } else {  
            	INSTRUCTION_MAP.get(0xB).execute(memory.getData(arg), level-1); 
            }
        });
        
        //INSTRUCTION_MAP entry for "JMPZ"
        //change the program counter to an immediate value or a value from memory, using direct addressing
        INSTRUCTION_MAP.put(0xC,(arg, level) -> {
            if(level < 0 || level > 1) {
                throw new IllegalArgumentException("Illegal indirection level in JMPZ instruction");
            }
            if (level == 0) {
            	if(cpu.accumulator == 0) {
                    cpu.programCounter = arg;
                } else {
                    cpu.programCounter++;               
                }            	           
            } else {
            	INSTRUCTION_MAP.get(0xC).execute(memory.getData(arg), level-1); 
            }
        });

        //INSTRUCTION_MAP entry for "NOT"
        //not operation
        INSTRUCTION_MAP.put(0x8,(arg, level) -> {
            if(level != 0) {
                throw new IllegalArgumentException("Illegal indirection level in NOT instruction");
            }
            
            if(cpu.accumulator == 0) {
                cpu.accumulator = 1;            
            } else {
                cpu.accumulator = 0;            
            }
            cpu.programCounter ++;          
        });
        
        //INSTRUCTION_MAP entry for "CMPZ"
        //compare zero: set the accumulator to “true” if the value in memory is 0, using direct addressing
       INSTRUCTION_MAP.put(0x9, (arg, level) -> {
    	   if (level != 1){
    		   throw new IllegalArgumentException("Illegel indirection level in CMPZ instruction");
    	   }
    	   
    	   if (memory.getData(arg) == 0){
    		   cpu.accumulator = 1;
    	   } else {
    		   cpu.accumulator = 0;
    	   }
    	   cpu.programCounter ++;    	       	       	       	   
       });

       //INSTRUCTION_MAP entry for "CMPL"
       //compare less than 0: set the accumulator to “true” if the value in memory is negative, using direct addressing
       INSTRUCTION_MAP.put(0xA, (arg,level) -> {
    	   if (level != 1){
    		   throw new IllegalArgumentException("Illegal indirection level in CMPL instruction");
    	   }
    	   
    	   if (memory.getData(arg) < 0){
    		   cpu.accumulator = 1;
    	   } else {
    		   cpu.accumulator = 0;
    	   }
    	   cpu.programCounter ++;    	   
       });
       
       //INSTRUCTION_MAP entry for ROT
       // Using direct addressing (So only works if level = 1), assigns start, length and move to the data in arg, arg+1, arg+2 respectively.
       //If start<0,length<0,start+length-1>= data size of memory, start<= arg+2, start+length-1<=arg, throw illegal
       //argument exception. Check is move is negative or positive. If negative, starting from the top(Start), use the accumulator
       // as a temp placeholder and move the data in the position before for whatever amount of |move| until start+length -1
       // is reached. If positive,starting from the last memory location(start+length-1), move each data up one memory
       // location until start is reached using accumulater as a temp place holder for whatever amount of move.
       INSTRUCTION_MAP.put(0x14, (arg, level) -> {
    	   if (level != 1){
    		   throw new IllegalArgumentException("Illegal indirection level in ROT instruction");
    	   }
    	   else{
	    	   int start = memory.getData(arg);
	    	   int length = memory.getData(arg+1);
	    	   int move = memory.getData(arg+2);
	    	   if (start < 0 || length < 0 || start+length-1 >= memory.DATA_SIZE || start <= arg+2 ||
	    			   start + length -1 <= arg){
	    		   throw new IllegalArgumentException("Values of start, length or move are not correct in ROT instruction");  
	    	   } else {
	    		   if (move < 0){
	    			   for(int i = move; i<0; i++){
	    				   cpu.accumulator = memory.getData(start);
	    				   for (int k=start; k<start+length-1; k++){
	    				   		memory.setData(k, memory.getData(k+1));
	    				   }
	    				   memory.setData(start+length-1, cpu.accumulator);
	    		   }
	    		   } else if (move > 0){
	    			   for (int i = move; i>0; i--){
	    				   cpu.accumulator = memory.getData(start+length-1);
	    				   for (int k=start+length-1; k > 0; k--){
	    					   memory.setData(k, memory.getData(k-1));
	    				   }
	    				   memory.setData(start, cpu.accumulator);
	    			   }
	    			   
	    			}
	    		  }
    	   		}
    	   		cpu.programCounter++;
    		  });
        
       //INSTRUCTION_MAP entry for "HALT"
       INSTRUCTION_MAP.put(0xF, (arg,level) -> {
    	   halt();
       });   
    }
    
    public MachineModel() {
    	this(false);
    }
  
}






