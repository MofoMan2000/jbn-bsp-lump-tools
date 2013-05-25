// SourceStaticProp class
// Handles the data needed for one static prop.
// This is the lump object with the most wild changes between different versions
// and different game implementations. More research needed

public class SourceStaticProp extends LumpObject {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	private Vector3D origin;
	private Vector3D angles;
	private short dictionaryEntry;
	private byte solidity;
	private byte flags;
	private int skin;
	private float minFadeDist;
	private float maxFadeDist;
	private float forcedFadeScale=1;
	String targetname=null;
	
	// CONSTRUCTORS
	public SourceStaticProp(LumpObject in, int type, int version) {
		super(in.getData());
		new SourceStaticProp(in.getData(), type, version);
	}

	public SourceStaticProp(byte[] data, int type, int version) {
		super(data);
		switch(type) {
			case BSP.TYPE_SOURCE17:
			case BSP.TYPE_SOURCE18:
			case BSP.TYPE_SOURCE19:
			case BSP.TYPE_SOURCE20:
			case BSP.TYPE_SOURCE21:
			case BSP.TYPE_SOURCE22:
			case BSP.TYPE_SOURCE23:
			case BSP.TYPE_TACTICALINTERVENTION:
			case BSP.TYPE_VINDICTUS:
			case BSP.TYPE_DMOMAM:
				switch(version) {
					case 5:
						if(data.length==188) { // This is only for The Ship or Bloody Good Time.
							byte[] targetnameBytes=new byte[128];
							for(int i=0;i<128;i++) {
								targetnameBytes[i]=data[60+i];
							}
							targetname=DataReader.readNullTerminatedString(targetnameBytes);
							if(targetname.length()==0) {
								targetname=null;
							}
						}
					case 6:
					case 7:
					case 8:
					case 9:
					case 10:
						forcedFadeScale=DataReader.readFloat(data[56], data[57], data[58], data[59]);
					case 4:
						origin=DataReader.readPoint3F(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11]);
						angles=DataReader.readPoint3F(data[12], data[13], data[14], data[15], data[16], data[17], data[18], data[19], data[20], data[21], data[22], data[23]);
						dictionaryEntry=DataReader.readShort(data[24], data[25]);
						solidity=data[30];
						flags=data[31];
						skin=DataReader.readInt(data[32], data[33], data[34], data[35]);
						minFadeDist=DataReader.readFloat(data[36], data[37], data[38], data[39]);
						maxFadeDist=DataReader.readFloat(data[40], data[41], data[42], data[43]);
					break;
				}
			break;
		}
	}
	
	// METHODS
	public static SourceStaticProps createLump(byte[] in, int type, int version) throws java.lang.InterruptedException {
		int structLength=0;
		String[] dictionary=new String[0];
		SourceStaticProp[] elements;
		if(in.length>0) {
			/*switch(type) { // It's possible to determine structlength using arithmetic rather than version numbering
				case BSP.TYPE_SOURCE17:
				case BSP.TYPE_SOURCE18:
				case BSP.TYPE_SOURCE19:
				case BSP.TYPE_SOURCE20:
				case BSP.TYPE_SOURCE21:
				case BSP.TYPE_SOURCE22:
				case BSP.TYPE_SOURCE23:
				switch(version) {
					case 4:
						structLength=56;
						break;
					case 5:
						structLength=60;
						break;
					case 6:
						structLength=64;
						break;
					case 7:
						structLength=68;
						break;
					case 8:
						structLength=72;
						break;
					case 9:
						structLength=73; // ??? The last entry is a boolean, is it stored as a byte?
						break;
					default:
						structLength=0;
						break;
				default:
					structLength=0;
			}*/
			int offset=0;
			dictionary=new String[DataReader.readInt(in[offset++], in[offset++], in[offset++], in[offset++])];
			for(int i=0;i<dictionary.length;i++) {
				byte[] temp=new byte[128];
				for(int j=0;j<128;j++) {
					temp[j]=in[offset++];
				}
				dictionary[i]=DataReader.readNullTerminatedString(temp);
			}
			int numLeafDefinitions=DataReader.readInt(in[offset++], in[offset++], in[offset++], in[offset++]);
			for(int i=0;i<numLeafDefinitions;i++) {
				offset+=2; // Each leaf index is an unsigned short, which i just want to skip
			}
			elements=new SourceStaticProp[DataReader.readInt(in[offset++], in[offset++], in[offset++], in[offset++])];
			if(elements.length>0) {
				structLength=(in.length-offset)/elements.length;
				byte[] bytes=new byte[structLength];
				for(int i=0;i<elements.length;i++) {
					if(Thread.currentThread().interrupted()) {
						throw new java.lang.InterruptedException("while populating SourceStaticProp array");
					}
					for(int j=0;j<structLength;j++) {
						bytes[j]=in[offset+j];
					}
					elements[i]=new SourceStaticProp(bytes, type, version);
					offset+=structLength;
				}
			}
		} else {
			elements=new SourceStaticProp[0];
		}
		return new SourceStaticProps(elements, dictionary, in.length);
	}
	
	// ACCESSORS/MUTATORS
	public Vector3D getOrigin() {
		return origin;
	}
	
	public Vector3D getAngles() {
		return angles;
	}
	
	public int getDictionaryEntry() {
		return dictionaryEntry;
	}
	
	public byte getSolidity() {
		return solidity;
	}
	
	public byte getFlags() {
		return flags;
	}
	
	public int getSkin() {
		return skin;
	}
	
	public float getMinFadeDist() {
		return minFadeDist;
	}
	
	public float getMaxFadeDist() {
		return maxFadeDist;
	}
	
	public float getForcedFadeScale() {
		return forcedFadeScale;
	}
	
	public String getTargetname() {
		return targetname;
	}
}