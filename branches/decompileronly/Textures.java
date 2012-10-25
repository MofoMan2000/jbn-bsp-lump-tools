// Textures class

// Maintains an array of Textures.

import java.io.FileInputStream;
import java.io.File;

public class Textures {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private File data;
	private int length;
	private Texture[] elements;
	
	private int structLength;

	// CONSTRUCTORS
	
	// Accepts a filepath as a String
	public Textures(String in, int type) {
		new Textures(new File(in), type);
	}
	
	// This one accepts the input file path as a File
	public Textures(File in, int type) {
		data=in;
		try {
			FileInputStream fileReader=new FileInputStream(data);
			byte[] temp=new byte[(int)data.length()];
			fileReader.read(temp);
			new Textures(temp, type);
			fileReader.close();
		} catch(java.io.FileNotFoundException e) {
			Window.println("ERROR: File "+data.getPath()+" not found!",Window.VERBOSITY_ALWAYS);
		} catch(java.io.IOException e) {
			Window.println("ERROR: File "+data.getPath()+" could not be read, ensure the file is not open in another program",Window.VERBOSITY_ALWAYS);
		}
	}
	
	// Takes a byte array, as if read from a FileInputStream
	public Textures(byte[] in, int type) {
		switch(type) {
			case Texture.TYPE_NIGHTFIRE:
				structLength=64;
				break;
			case Texture.TYPE_QUAKE3:
				structLength=72;
				break;
			case Texture.TYPE_QUAKE2:
			case Texture.TYPE_EF2:
				structLength=76;
				break;
			case Texture.TYPE_MOHAA:
				structLength=140;
				break;
			case Texture.TYPE_SIN:
				structLength=180;
				break;
			case Texture.TYPE_SOURCE:
				; // TODO
				break;
			case Texture.TYPE_QUAKE:
				; // TODO
				break;
			default:
				structLength=0; // This will cause the shit to hit the fan.
		}
		int offset=0;
		length=in.length;
		elements=new Texture[in.length/structLength];
		byte[] bytes=new byte[structLength];
		for(int i=0;i<elements.length;i++) {
			for(int j=0;j<structLength;j++) {
				bytes[j]=in[offset+j];
			}
			elements[i]=new Texture(bytes, type);
			offset+=structLength;
		}
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	// Returns the length (in bytes) of the lump
	public int getLength() {
		return length;
	}
	
	// Returns the number of elements.
	public int length() {
		if(elements.length==0) {
			return length/structLength;
		} else {
			return elements.length;
		}
	}
	
	public Texture getElement(int i) {
		return elements[i];
	}
	
	public Texture[] getElements() {
		return elements;
	}
}