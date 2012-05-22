// DSidedef class
// Contains all necessary information for a Doom SIDEDEF object.
// The sidedef is roughly equivalent to the Face (or surface)
// object in later BSP versions.
// This is one of the cleverest, dumbest, craziest structures
// I've ever seen in a game map format. It contains three texture
// references, and how they are used depends on adjacent sectors.

public class DSidedef {

	// INITIAL DATA DEFINITION AND DECLARATION OF CONSTANTS

	private short[] offsets;
	private String[] textures;
	private short sector;
	
	public static final int HIGH=0;
	public static final int MID=1;
	public static final int LOW=2;
	
	public static final int X=0;
	public static final int Y=1;
	
	// CONSTRUCTORS
	
	public DSidedef(short xoffset, short yoffset, String highTexture, String midTexture, String lowTexture, short sector) {
		offsets=new short[2];
		this.offsets[X]=xoffset;
		this.offsets[Y]=yoffset;
		textures=new String[3];
		this.textures[HIGH]=highTexture;
		this.textures[MID]=midTexture;
		this.textures[LOW]=lowTexture;
		this.sector=sector;
	}
	
	public DSidedef(byte[] in) {
		offsets=new short[2];
		offsets[X]=DataReader.readShort(in[0], in[1]);
		offsets[Y]=DataReader.readShort(in[2], in[3]);
		textures=new String[3];
		for(int i=0;i<3;i++) {
			textures[i]="";
			for(int j=0;j<8;j++) {
				if(in[(i*8)+j+4] != (byte)0x00) {
					textures[i]+=(char)in[(i*8)+j+4];
				} else {
					break;
				}
			}
		}
		sector=DataReader.readShort(in[28], in[29]);
	}
	
	// METHODS
	
	// ACCESSORS AND MUTATORS
	
	public short getOffsetX() {
		return offsets[X];
	}
	
	public void setOffsetX(short in) {
		offsets[X]=in;
	}
	
	public short getOffsetY() {
		return offsets[Y];
	}
	
	public void setOffsetY(short in) {
		offsets[Y]=in;
	}
	
	public String getHighTexture() {
		return textures[HIGH];
	}
	
	public void setHighTexture(String in) {
		textures[HIGH]=in;
	}
	
	public String getMidTexture() {
		return textures[MID];
	}
	
	public void setMidTexture(String in) {
		textures[MID]=in;
	}
	
	public String getLowTexture() {
		return textures[LOW];
	}
	
	public void setLowTexture(String in) {
		textures[LOW]=in;
	}
	
	public short getSector() {
		return sector;
	}
	
	public void setSector(short in) {
		sector=in;
	}
}