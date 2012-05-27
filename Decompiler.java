// Decompiler class

// Handles the actual decompilation.

import java.util.Date;
import java.util.Scanner;

public class Decompiler {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public static final int A = 0;
	public static final int B = 1;
	public static final int C = 2;
	
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	
	// These are lowercase so as not to conflict with A B and C
	// Light entity attributes; red, green, blue, strength (can't use i for intensity :P)
	public static final int r = 0;
	public static final int g = 1;
	public static final int b = 2;
	public static final int s = 3;
	
	private boolean vertexDecomp;
	private boolean correctPlaneFlip;
	private double planePointCoef;
	private boolean toVMF;
	private boolean calcVerts;
	private boolean roundNums;
	
	private Lump00 mapFile; // Most MAP file formats (including GearCraft) are simply a bunch of nested entities
	private int numBrshs;
	
	// Declare all kinds of BSPs here, the one actually used will be determined by constructor
	private NFBSP BSP42;
	
	// CONSTRUCTORS
	
	// This constructor sets everything according to specified settings.
	public Decompiler(NFBSP BSP42, boolean vertexDecomp, boolean correctPlaneFlip, boolean calcVerts, boolean roundNums, boolean toVMF, double planePointCoef) {
		// Set up global variables
		this.BSP42=BSP42;
		this.vertexDecomp=vertexDecomp;
		this.correctPlaneFlip=correctPlaneFlip;
		this.planePointCoef=planePointCoef;
		this.toVMF=toVMF;
		this.calcVerts=calcVerts;
		this.roundNums=roundNums;
	}
	
	// METHODS
	
	// +decompile()
	// Starts the decompilation process. This is leftover from when multithreading
	// was handled through the decompiler.
	public void decompile() throws java.io.IOException {
		Date begin=new Date();
		decompileBSP42();
		Date end=new Date();
		System.out.println("Time taken: "+(end.getTime()-begin.getTime())+"ms\n");
	}
	
	// -decompileBSP42()
	// Attempts to convert the Nightfire BSP file back into a .MAP file.
	//
	// This is another one of the most complex things I've ever had to code. I've
	// never nested for loops four deep before.
	// Iterators:
	// i: Current entity in the list
	//  j: Current leaf, referenced in a list by the model referenced by the current entity
	//   k: Current brush, referenced in a list by the current leaf.
	//    l: Current side of the current brush.
	//     m: When attempting vertex decompilation, the current vertex.
	public void decompileBSP42() throws java.io.IOException {
		// Begin by copying all the entities into another Lump00 object. This is
		// necessary because if I just modified the current entity list then it
		// could be saved back into the BSP and really mess some stuff up.
		mapFile=new Lump00(BSP42.getLump00());
		int numTotalItems=0;
		// Next make a list of all detail brushes.
		boolean[] detailBrush=new boolean[BSP42.getLump15().getNumElements()];
		for(int i=0;i<BSP42.getLump15().getNumElements();i++) {	// For every brush
			if(BSP42.getLump15().getBrush(i).getAttributes()[1]==0x02) { // This attribute seems to indicate no affect on vis
				detailBrush[i]=true; // Flag the brush as detail
			}
		}
		// Then I need to go through each entity and see if it's brush-based.
		// Worldspawn is brush-based as well as any entity with model *#.
		for(int i=0;i<BSP42.getLump00().getNumElements();i++) { // For each entity
			if(toVMF) { // correct some entities to make source ports easier, TODO add more
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("light_spot")) {
					mapFile.getEntity(i).setAttribute("pitch", new Double(mapFile.getEntity(i).getAngles()[0]).toString());
					mapFile.getEntity(i).renameAttribute("_cone", "_inner_cone"); 
					mapFile.getEntity(i).renameAttribute("_cone2", "_cone");
					try {
						if(Double.parseDouble(mapFile.getEntity(i).getAttribute("_cone"))>90.0) {
							mapFile.getEntity(i).setAttribute("_cone", "90");
						} else {
							if(Double.parseDouble(mapFile.getEntity(i).getAttribute("_cone"))<0.0) {
								mapFile.getEntity(i).setAttribute("_cone", "0");
							}
						}
						if(Double.parseDouble(mapFile.getEntity(i).getAttribute("_cone2"))>90.0) {
							mapFile.getEntity(i).setAttribute("_cone2", "90");
						} else {
							if(Double.parseDouble(mapFile.getEntity(i).getAttribute("_cone2"))<0.0) {
								mapFile.getEntity(i).setAttribute("_cone2", "0");
							}
						}
					} catch(java.lang.NumberFormatException e) {
						;
					}
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("func_wall")) {
					if(mapFile.getEntity(i).getAttribute("rendermode").equals("0")) {
						mapFile.getEntity(i).setAttribute("classname", "func_detail");
						mapFile.getEntity(i).deleteAttribute("rendermode");
					} else {
						mapFile.getEntity(i).setAttribute("classname", "func_brush");
						mapFile.getEntity(i).setAttribute("Solidity", "2");
					}
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("func_wall_toggle")) {
					mapFile.getEntity(i).setAttribute("classname", "func_brush");
					mapFile.getEntity(i).setAttribute("Solidity", "0");
					try {
						if((Double.parseDouble(mapFile.getEntity(i).getAttribute("spawnflags")))/2.0 == Double.parseDouble(mapFile.getEntity(i).getAttribute("spawnflags"))) {
							mapFile.getEntity(i).setAttribute("StartDisabled", "1"); // If spawnflags is an odd number, the start disabled flag is set.
						} else {
							mapFile.getEntity(i).setAttribute("StartDisabled", "0");
						}
					} catch(java.lang.NumberFormatException e) {
						mapFile.getEntity(i).setAttribute("StartDisabled", "0");
					}
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("func_illusionary")) {
					mapFile.getEntity(i).setAttribute("classname", "func_brush");
					mapFile.getEntity(i).setAttribute("Solidity", "1");
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("item_generic")) {
					mapFile.getEntity(i).setAttribute("classname", "prop_dynamic");
					mapFile.getEntity(i).setAttribute("solid", "0");
					mapFile.getEntity(i).deleteAttribute("effects");
					mapFile.getEntity(i).deleteAttribute("fixedlight");
				}
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("env_glow")) {
					mapFile.getEntity(i).setAttribute("classname", "env_sprite");
				}
				if(!mapFile.getEntity(i).getAttribute("body").equalsIgnoreCase("")) {
					mapFile.getEntity(i).renameAttribute("body", "SetBodyGroup");
				}
				if(mapFile.getEntity(i).getAttribute("rendercolor").equals("0 0 0")) {
					mapFile.getEntity(i).setAttribute("rendercolor", "255 255 255");
				}
				try {
					if(mapFile.getEntity(i).getAttribute("model").substring(mapFile.getEntity(i).getAttribute("model").length()-4).equalsIgnoreCase(".spz")) {
						mapFile.getEntity(i).setAttribute("model", mapFile.getEntity(i).getAttribute("model").substring(0, mapFile.getEntity(i).getAttribute("model").length()-4)+".spr");
					}
				} catch(java.lang.StringIndexOutOfBoundsException e) {
					;
				}
			} else { // Gearcraft needs a couple things, too. These things usually make it into the compiled map, but just in case.
				if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("worldspawn")) {
					mapFile.getEntity(i).setAttribute("mapversion", "510"); // Otherwise Gearcraft cries.
				}
			}
			numBrshs=0; // Reset the brush count for each entity
			// getModelNumber() returns 0 for worldspawn, the *# for brush based entities, and -1 for everything else
			int currentModel=mapFile.getEntity(i).getModelNumber();
			
			if(currentModel!=-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getEntity(i).getOrigin();
				int firstLeaf=BSP42.getLump14().getModel(currentModel).getLeaf();
				int numLeaves=BSP42.getLump14().getModel(currentModel).getNumLeafs();
				boolean[] brushesUsed=new boolean[BSP42.getLump15().getNumElements()]; // Keep a list of brushes already in the model, since sometimes the leaves lump references one brush several times
				numBrshs=0;
				for(int j=0;j<numLeaves;j++) { // For each leaf in the bunch
					Leaf currentLeaf=BSP42.getLump11().getLeaf(j+firstLeaf);
					int firstBrushIndex=currentLeaf.getMarkBrush();
					int numBrushIndices=currentLeaf.getNumMarkBrushes();
					if(numBrushIndices>0) { // A lot of leaves reference no brushes. If this is one, this iteration of the j loop is finished
						for(int k=0;k<numBrushIndices;k++) { // For each brush referenced
							if(!brushesUsed[BSP42.getLump13().getMarkBrush(firstBrushIndex+k)]) { // If the current brush has NOT been used in this entity
								brushesUsed[BSP42.getLump13().getMarkBrush(firstBrushIndex+k)]=true;
								if(detailBrush[BSP42.getLump13().getMarkBrush(firstBrushIndex+k)] && currentModel==0) {
									decompileBrush42(BSP42.getLump15().getBrush(BSP42.getLump13().getMarkBrush(firstBrushIndex+k)), i, true); // Decompile the brush, as not detail
								} else {
									decompileBrush42(BSP42.getLump15().getBrush(BSP42.getLump13().getMarkBrush(firstBrushIndex+k)), i, false); // Decompile the brush, as detail
								}
								numBrshs++;
								numTotalItems++;
							}
						}
					}
				}
				mapFile.getEntity(i).deleteAttribute("model");
				// Recreate origin brushes for entities that need them, only for GearCraft though.
				// These are discarded on compile and replaced with an "origin" attribute in the entity.
				// I need to undo that. For this I will create a 32x32 brush, centered at the point defined
				// by the "origin" attribute. Hammer keeps the "origin" attribute and uses it directly
				// instead, so we'll keep it in a VMF.
				if(!toVMF) {
					if(origin[0]!=0 || origin[1]!=0 || origin[2]!=0) { // If this brush uses the "origin" attribute
						addOriginBrush(i);
					}
					//mapFile.getEntity(i).deleteAttribute("origin");
				}
			}
			numTotalItems++;
		}
		if(toVMF) {
			System.out.println("Saving "+BSP42.getPath().substring(0, BSP42.getPath().length()-4)+".vmf...");
			VMFWriter VMFMaker=new VMFWriter(mapFile, BSP42.getPath().substring(0, BSP42.getPath().length()-4), roundNums);
			VMFMaker.write();
		} else {
			System.out.println("Saving "+BSP42.getPath().substring(0, BSP42.getPath().length()-4)+".map...");
			MAP510Writer MAPMaker=new MAP510Writer(mapFile, BSP42.getPath().substring(0, BSP42.getPath().length()-4), roundNums);
			MAPMaker.write();
		}
		System.out.println("Process completed!");
	}
	
	// -decompileBrush42(Brush, int, boolean)
	// Decompiles the Brush and adds it to entitiy #currentEntity as .MAP data.
	private void decompileBrush42(Brush brush, int currentEntity, boolean isDetailBrush) {
		double[] origin=mapFile.getEntity(currentEntity).getOrigin();
		int firstSide=brush.getFirstSide();
		int numSides=brush.getNumSides();
		MAPBrushSide[] brushSides=new MAPBrushSide[numSides];
		MAPBrush mapBrush = new MAPBrush(numBrshs, currentEntity, planePointCoef, isDetailBrush);
		int numRealFaces=0;
		boolean containsNonClipSide=false;
		for(int l=0;l<numSides;l++) { // For each side of the brush
			BrushSide currentSide=BSP42.getLump16().getBrushSide(firstSide+l);
			Face currentFace=BSP42.getLump09().getFace(currentSide.getFace()); // To find those three points, I can use vertices referenced by faces.
			String texture=BSP42.getLump02().getTexture(currentFace.getTexture());
			if(currentFace.getType()!=800) { // These surfaceflags (512 + 256 + 32) are set only by the compiler, on faces that need to be thrown out.
				if(!texture.equalsIgnoreCase("special/clip") && !texture.equalsIgnoreCase("special/playerclip") && !texture.equalsIgnoreCase("special/enemyclip")) {
					containsNonClipSide=true;
				}
				int firstVertex=currentFace.getVert();
				int numVertices=currentFace.getNumVerts();
				Plane currentPlane;
				try {
					currentPlane=BSP42.getLump01().getPlane(currentSide.getPlane());
				} catch(java.lang.ArrayIndexOutOfBoundsException e) {
					try {
						currentPlane=BSP42.getLump01().getPlane(currentFace.getPlane());
					}  catch(java.lang.ArrayIndexOutOfBoundsException f) {
						System.out.println("WARNING: BSP has error, references nonexistant plane "+currentSide.getPlane()+", bad side "+(l)+" of brush "+numBrshs+" Entity "+currentEntity);
						currentPlane=new Plane((double)1, (double)0, (double)0, (double)0, 0);
					}
				}
				Vector3D[] triangle=new Vector3D[0];
				if(numVertices!=0 && vertexDecomp) { // If the face actually references a set of vertices
					triangle=new Vector3D[3]; // Three points define a plane. All I have to do is find three points on that plane.
					triangle[0]=new Vector3D(BSP42.getLump04().getVertex(firstVertex)); // Grab and store the first one
					int m=1;
					for(m=1;m<numVertices;m++) { // For each point after the first one
						triangle[1]=new Vector3D(BSP42.getLump04().getVertex(firstVertex+m));
						if(!triangle[0].equals(triangle[1])) { // Make sure the point isn't the same as the first one
							break; // If it isn't the same, this point is good
						}
					}
					for(m=m+1;m<numVertices;m++) { // For each point after the previous one used
						triangle[2]=new Vector3D(BSP42.getLump04().getVertex(firstVertex+m));
						if(!triangle[2].equals(triangle[0]) && !triangle[2].equals(triangle[1])) { // Make sure no point is equal to the third one
							if((Vector3D.crossProduct(triangle[0].subtract(triangle[1]), triangle[0].subtract(triangle[2])).getX()!=0) || // Make sure all
							   (Vector3D.crossProduct(triangle[0].subtract(triangle[1]), triangle[0].subtract(triangle[2])).getY()!=0) || // three points 
							   (Vector3D.crossProduct(triangle[0].subtract(triangle[1]), triangle[0].subtract(triangle[2])).getZ()!=0)) { // are not collinear
								break;
							}
						}
					}
				}
				// Correct texture names for Source engine
				if(toVMF) {
					if(texture.equalsIgnoreCase("special/nodraw")) {
						texture="tools/toolsnodraw";
					} else {
						if(texture.equalsIgnoreCase("special/clip")) {
							texture="tools/toolsclip";
						} else {
							if(texture.equalsIgnoreCase("special/sky")) {
								texture="tools/toolsskybox";
							} else {
								if(texture.equalsIgnoreCase("special/trigger")) {
									texture="tools/toolstrigger";
								} else {
									if(texture.equalsIgnoreCase("special/playerclip")) {
										texture="tools/toolsplayerclip";
									} else {
										if(texture.equalsIgnoreCase("special/npcclip") || texture.equalsIgnoreCase("special/enemyclip")) {
											texture="tools/toolsnpcclip";
										}
									}
								}
							}
						}
					}
				}
				double[] textureS=new double[3];
				double[] textureT=new double[3];
				TexMatrix currentTexMatrix=BSP42.getLump17().getTexMatrix(currentFace.getTexStyle());
				// Get the lengths of the axis vectors
				double UAxisLength=Math.sqrt(Math.pow((double)currentTexMatrix.getUAxisX(),2)+Math.pow((double)currentTexMatrix.getUAxisY(),2)+Math.pow((double)currentTexMatrix.getUAxisZ(),2));
				double VAxisLength=Math.sqrt(Math.pow((double)currentTexMatrix.getVAxisX(),2)+Math.pow((double)currentTexMatrix.getVAxisY(),2)+Math.pow((double)currentTexMatrix.getVAxisZ(),2));
				// In compiled maps, shorter vectors=longer textures and vice versa. This will convert their lengths back to 1. We'll use the actual scale values for length.
				double texScaleS=(1/UAxisLength);// Let's use these values using the lengths of the U and V axes we found above.
				double texScaleT=(1/VAxisLength);
				textureS[0]=((double)currentTexMatrix.getUAxisX()/UAxisLength);
				textureS[1]=((double)currentTexMatrix.getUAxisY()/UAxisLength);
				textureS[2]=((double)currentTexMatrix.getUAxisZ()/UAxisLength);
				double originShiftS=(((double)currentTexMatrix.getUAxisX()/UAxisLength)*origin[X]+((double)currentTexMatrix.getUAxisY()/UAxisLength)*origin[Y]+((double)currentTexMatrix.getUAxisZ()/UAxisLength)*origin[Z])/texScaleS;
				double textureShiftS=(double)currentTexMatrix.getUShift()-originShiftS;
				textureT[0]=((double)currentTexMatrix.getVAxisX()/VAxisLength);
				textureT[1]=((double)currentTexMatrix.getVAxisY()/VAxisLength);
				textureT[2]=((double)currentTexMatrix.getVAxisZ()/VAxisLength);
				double originShiftT=(((double)currentTexMatrix.getVAxisX()/VAxisLength)*origin[X]+((double)currentTexMatrix.getVAxisY()/VAxisLength)*origin[Y]+((double)currentTexMatrix.getVAxisZ()/VAxisLength)*origin[Z])/texScaleT;
				double textureShiftT=(double)currentTexMatrix.getVShift()-originShiftT;
				float texRot=0; // In compiled maps this is calculated into the U and V axes, so set it to 0 until I can figure out a good way to determine a better value.
				int flags=currentFace.getType(); // This is actually a set of flags. Whatever.
				String material;
				try {
					material=BSP42.getLump03().getMaterial(currentFace.getMaterial());
				} catch(java.lang.ArrayIndexOutOfBoundsException e) { // In case the BSP has some strange error making it reference nonexistant materials
					System.out.println("WARNING: Map referenced nonexistant material #"+currentFace.getMaterial()+", using wld_lightmap instead!");
					material="wld_lightmap";
				}
				double lgtScale=16; // These values are impossible to get from a compiled map since they
				double lgtRot=0;    // are used by RAD for generating lightmaps, then are discarded, I believe.
				if(triangle.length==0) {
					brushSides[l]=new MAPBrushSide(currentPlane, texture, textureS, textureShiftS, textureT, textureShiftT,
					                               texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
				} else {
					brushSides[l]=new MAPBrushSide(currentPlane, triangle, texture, textureS, textureShiftS, textureT, textureShiftT,
					                               texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
				}
				numRealFaces++;
				mapBrush.add(brushSides[l]); // Add the MAPBrushSide to the current brush
			}
		}
		
		// TODO: FIX FOR MY NEW CODE
		if(correctPlaneFlip) {
			//if(mapBrush.hasGoodSide() && mapBrush.hasBadSide()) {
			//	mapBrush=GenericMethods.SimpleCorrectPlanes(mapBrush, 0.01); // This is good.
			//	if(calcVerts) { // So, only allow this if vertex decompile.
			//		mapBrush.recalcCorners();
			//	}
			//} else {
			//	if(mapBrush.hasBadSide()) {
			//		mapBrush.correctPlanes(); // This is bad.
			//	}
			//}
		}
		
		// This adds the brush we've been finding and creating to
		// the current entity as an attribute. The way I've coded
		// this whole program and the entities parser, this shouldn't
		// cause any issues at all.
		if(toVMF && isDetailBrush && containsNonClipSide) {
			Entity newDetailEntity=new Entity("func_detail");
			newDetailEntity.addBrush(mapBrush);
			mapFile.add(newDetailEntity);
		} else {
			mapFile.getEntity(currentEntity).addBrush(mapBrush);
		}
	}
	
	public void addOriginBrush(int ent) {
		double[] origin=new double[3];
		MAPBrush newOriginBrush=new MAPBrush(numBrshs++, ent, 0, false);
		Vector3D[][] planes=new Vector3D[6][3]; // Six planes for a cube brush, three vertices for each plane
		double[][] textureS=new double[6][3];
		double[][] textureT=new double[6][3];
		// The planes and their texture scales
		// I got these from an origin brush created by Gearcraft. Don't worry where these numbers came from, they work.
		// Top
		planes[0][0]=new Vector3D(-16+origin[0], 16+origin[1], 16+origin[2]);
		planes[0][1]=new Vector3D(16+origin[0], 16+origin[1], 16+origin[2]);
		planes[0][2]=new Vector3D(16+origin[0], -16+origin[1], 16+origin[2]);
		textureS[0][0]=1;
		textureT[0][1]=-1;
		// Bottom
		planes[1][0]=new Vector3D(-16+origin[0], -16+origin[1], -16+origin[2]);
		planes[1][1]=new Vector3D(16+origin[0], -16+origin[1], -16+origin[2]);
		planes[1][2]=new Vector3D(16+origin[0], 16+origin[1], -16+origin[2]);
		textureS[1][0]=1;
		textureT[1][1]=-1;
		// Left
		planes[2][0]=new Vector3D(-16+origin[0], 16+origin[1], 16+origin[2]);
		planes[2][1]=new Vector3D(-16+origin[0], -16+origin[1], 16+origin[2]);
		planes[2][2]=new Vector3D(-16+origin[0], -16+origin[1], -16+origin[2]);
		textureS[2][1]=1;
		textureT[2][2]=-1;
		// Right
		planes[3][0]=new Vector3D(16+origin[0], 16+origin[1], -16+origin[2]);
		planes[3][1]=new Vector3D(16+origin[0], -16+origin[1], -16+origin[2]);
		planes[3][2]=new Vector3D(16+origin[0], -16+origin[1], 16+origin[2]);
		textureS[3][1]=1;
		textureT[3][2]=-1;
		// Near
		planes[4][0]=new Vector3D(16+origin[0], 16+origin[1], 16+origin[2]);
		planes[4][1]=new Vector3D(-16+origin[0], 16+origin[1], 16+origin[2]);
		planes[4][2]=new Vector3D(-16+origin[0], 16+origin[1], -16+origin[2]);
		textureS[4][0]=1;
		textureT[4][2]=-1;
		// Far
		planes[5][0]=new Vector3D(16+origin[0], -16+origin[1], -16+origin[2]);
		planes[5][1]=new Vector3D(-16+origin[0], -16+origin[1], -16+origin[2]);
		planes[5][2]=new Vector3D(-16+origin[0], -16+origin[1], 16+origin[2]);
		textureS[5][0]=1;
		textureT[5][2]=-1;
		
		for(int j=0;j<6;j++) {
			MAPBrushSide currentEdge;
			if(toVMF) {
				currentEdge=new MAPBrushSide(planes[j], "tools/toolsorigin", textureS[j], 0, textureT[j], 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			} else {
				currentEdge=new MAPBrushSide(planes[j], "special/origin", textureS[j], 0, textureT[j], 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			}
			newOriginBrush.add(currentEdge);
		}
		mapFile.getEntity(ent).addBrush(newOriginBrush);
	}
}