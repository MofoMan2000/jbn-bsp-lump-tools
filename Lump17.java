// Lump17 class

// Holds the information for texture scaling and alignment.
// Referenced only by faces. The data contained here could
// potentially be recycled.

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.*;

public class Lump17 {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int numTxmatxs=0; // I really don't know what to call this lump
	private TexMatrix[] texturematrix;
	
	// CONSTRUCTORS
	
	// This one accepts the lump path as a String
	public Lump17(String in) throws java.io.FileNotFoundException, java.io.IOException {
		data=new File(in);
		numTxmatxs=getNumElements();
		texturematrix=new TexMatrix[numTxmatxs];
		populateTextureMatrixList();
	}
	
	// This one accepts the input file path as a File
	public Lump17(File in) throws java.io.FileNotFoundException, java.io.IOException {
		data=in;
		numTxmatxs=getNumElements();
		texturematrix=new TexMatrix[numTxmatxs];
		populateTextureMatrixList();
	}
	
	// METHODS
	
	// -populateTextureMatrixList()
	// Aw, fuck, if you don't know what this does by now then look at
	// the other lump classes.
	private void populateTextureMatrixList() throws java.io.FileNotFoundException, java.io.IOException {
		FileInputStream reader=new FileInputStream(data);
		for(int i=0;i<numTxmatxs;i++) {
			byte[] datain=new byte[32];
			reader.read(datain);
			texturematrix[i]=new TexMatrix(datain);
		}
		reader.close();
	}
	
	// Accessors/mutators
	
	// Returns the length (in bytes) of the lump
	public long getLength() {
		return data.length();
	}
	
	// Returns the number of texture scales.
	public int getNumElements() {
		if(numTxmatxs==0) {
			return (int)data.length()/32;
		} else {
			return numTxmatxs;
		}
	}
}