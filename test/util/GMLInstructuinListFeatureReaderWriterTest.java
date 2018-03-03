package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.junit.Test;

import malware.parse.AsmParser.Instruction;

import static org.junit.Assert.*;

public class GMLInstructuinListFeatureReaderWriterTest {

  @Test
  public void testReaderWrite() {
    List<Instruction> instList = new ArrayList<Instruction>();
    instList.add(new Instruction("mov", "4A"));
    instList.add(new Instruction("mov", "4a"));
    instList.add(new Instruction("jmp", "ed"));
    instList.add(new Instruction("call", "09"));
    instList.add(new Instruction("test", "0F 4A"));
    instList.add(new Instruction("add", "99"));
    
    GMLInstructuinListFeatureReaderWriter author = new GMLInstructuinListFeatureReaderWriter();
    
    String buff = author.write(instList, "\t");
    
    System.out.println(buff);
    
    Pattern endPat = Pattern.compile("\\s*\\]\\s*");
    List<Instruction> readInst = author.read(new Scanner(buff), endPat);
    
    assertEquals("Instruction list size", instList.size(), readInst.size());
    for (int i = 0; i < instList.size(); i++) {
      assertEquals(instList.get(i).getAsm(), readInst.get(i).getAsm());
      assertEquals(instList.get(i).getOpcode(), readInst.get(i).getOpcode());
    }
  }

}
