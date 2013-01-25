// DoomMap class
// This class gathers all relevant information from the lumps of a Doom Map.
// I don't know if I can call this a BSP, though. It's more of a Binary Area
// Partition, a BAP.
// Anyhow, it never had a formal BSP version number, nor was it ever referred
// to as a BSP, so it's DoomMap.

import java.io.File;

public class DoomMap {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// This is the version of the BSP. This will determine the lumps order and aid in
	// decompilation. Since this one never had a formal version, I'll make one up. 1
	// is the most correct version since it really was the first version...
	public static final int TYPE_DOOM=1;
	public static final int TYPE_HEXEN=2;
	
	// Since all Doom engine maps were incorporated into the WAD, we need to keep
	// track of both the location of the WAD file and the internal name of the map.
	private String wadpath;
	private String mapName;
	private int version;
	
	// Each lump has its own class for handling its specific data structures.
	// These are the only lumps we need for decompilation.
	private DThings things;
	private DLinedefs linedefs;
	private DSidedefs sidedefs;
	private Vertices vertices;
	private DSegments segs;
	private DSubSectors subsectors;
	private DNodes nodes;
	private DSectors sectors;
	
	// CONSTRUCTORS
	// This accepts a folder path and looks for the BSP there.
	public DoomMap(String wadpath, String map) {
		this.wadpath=wadpath;
		if(map.charAt(0)=='E') { // ExMy format - 4 chars
			this.mapName=map.substring(0,4);
		} else { // MAPxx format - 5 chars
			this.mapName=map;
		}
	}

	// METHODS

	public void printBSPReport() {
		try {
			Window.println("Things lump: "+things.getLength()+" bytes, "+things.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Things not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Linedefs lump: "+linedefs.getLength()+" bytes, "+linedefs.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Linedefs not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Sidedefs lump: "+sidedefs.getLength()+" bytes, "+sidedefs.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Sidedefs not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Vertices lump: "+vertices.getLength()+" bytes, "+vertices.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Vertices not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Segments lump: "+segs.getLength()+" bytes, "+segs.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Segments not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Subsectors lump: "+subsectors.getLength()+" bytes, "+subsectors.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Subsectors not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Nodes lump: "+nodes.getLength()+" bytes, "+nodes.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Nodes not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
		try {
			Window.println("Sectors lump: "+sectors.getLength()+" bytes, "+sectors.length()+" items",Window.VERBOSITY_MAPSTATS);
		} catch(java.lang.NullPointerException e) {
			Window.println("Sectors not yet parsed!",Window.VERBOSITY_MAPSTATS);
		}
	}
	
	// ACCESSORS/MUTATORS
	
	public int getVersion() {
		return version;
	}
	
	public String getPath() {
		return wadpath;
	}
	
	public String getMapName() {
		return mapName;
	}
	
	public String getFolder() {
		int i;
		for(i=0;i<wadpath.length();i++) {
			if(wadpath.charAt(wadpath.length()-1-i)=='\\') {
				break;
			}
			if(wadpath.charAt(wadpath.length()-1-i)=='/') {
				break;
			}
		}
		return wadpath.substring(0,wadpath.length()-i);
	}
	
	public String getWadName() {
		File newFile=new File(wadpath);
		return newFile.getName().substring(0, newFile.getName().length()-4);
	}
	
	public void setThings(byte[] data, int type) throws java.lang.InterruptedException {
		if(version==0) {
			version=type;
		}
		things=new DThings(data, type);
	}
	
	public DThings getThings() {
		return things;
	}
	
	public void setLinedefs(byte[] data, int type) throws java.lang.InterruptedException {
		if(version==0) {
			version=type;
		}
		linedefs=new DLinedefs(data, type);
	}
	
	public DLinedefs getLinedefs() {
		return linedefs;
	}
	
	public void setSidedefs(byte[] data) throws java.lang.InterruptedException {
		sidedefs=new DSidedefs(data);
	}
	
	public DSidedefs getSidedefs() {
		return sidedefs;
	}
	
	public void setVertices(byte[] data) throws java.lang.InterruptedException {
		vertices=new Vertices(data, TYPE_DOOM);
	}
	
	public Vertices getVertices() {
		return vertices;
	}
	
	public void setSegments(byte[] data) throws java.lang.InterruptedException {
		segs=new DSegments(data);
	}
	
	public DSegments getSegments() {
		return segs;
	}
	
	public void setSubSectors(byte[] data) throws java.lang.InterruptedException {
		subsectors=new DSubSectors(data);
	}
	
	public DSubSectors getSubSectors() {
		return subsectors;
	}
	
	public void setNodes(byte[] data) throws java.lang.InterruptedException {
		nodes=new DNodes(data);
	}
	
	public DNodes getNodes() {
		return nodes;
	}
	
	public void setSectors(byte[] data) throws java.lang.InterruptedException {
		sectors=new DSectors(data);
	}
	
	public DSectors getSectors() {
		return sectors;
	}
}
