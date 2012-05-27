// This class holds useful methods and functions, which can be used if required.
// Some of these may not be necessary at all for decompiling, but they are available if required.

public class GenericMethods {
	
	// Calculates 3 face corners, to be used to define the plane in ASCII format.
	/// Author:		UltimateSniper
	/// Returns:	List of normalised plane vertex triplets.
	public static Vector3D[][] CalcPlanePoints(Plane[] planes, float pmprecision) {
		Vector3D[][] out = new Vector3D[planes.length][];
		// For each triplet of planes, find intersect point.
		for (int iP1 = 0; iP1 < planes.length; iP1++) {
			for (int iP2 = iP1 + 1; iP2 < planes.length; iP2++) {
				for (int iP3 = iP2 + 1; iP3 < planes.length; iP3++) {
					Vector3D testV = planes[iP1].trisect(planes[iP2], planes[iP3]);
					if (!testV.equals(Vector3D.undefined)) {
						boolean isCorner = true;
						// If point is not null, test if point is behind/on all planes (if so, it is a corner).
						for (int iTest = 0; iTest < planes.length; iTest++) {
							if (!planes[iTest].getNormal().equals(planes[iP1].getNormal()) && !planes[iTest].getNormal().equals(planes[iP2].getNormal()) && !planes[iTest].getNormal().equals(planes[iP3].getNormal())) {
								if (planes[iTest].distance(testV) > pmprecision) {
									isCorner = false;
									break;
								}
							}
						}
						// If so, check which planes it is on.
						if (isCorner) {
							for (int iChk = 0; iChk < planes.length; iChk++) {
								// If on this plane, and plane's vertex triplet missing min 1 point (and does not already have this point), add it.
								double dist = planes[iChk].distance(testV);
								if (dist <= pmprecision && dist >= -pmprecision) {
									// If first point on this plane, must create array.
									if (out[iChk] == null) {
										out[iChk] = new Vector3D[] { new Vector3D(testV) , null , null };
									} else {
										// Check each value in the array for open spot OR identical point.
										for (int iChk2 = 0; iChk2 < 3; iChk2++) {
											// Open spot, fill it.
											if (out[iChk][iChk2] == null) {
												out[iChk][iChk2] = new Vector3D(testV);
												// If this is now a complete plane.
												if (iChk2 == 2) {
													// Order complete triplet to make a plane facing same way as given plane.
													Plane testP = new Plane(out[iChk][0], out[iChk][1], out[iChk][2]);
													// If normals are not pointing in same direction, re-order points.
													if (testP.getNormal().dot(planes[iChk].getNormal()) < 0) {
														Vector3D temp = new Vector3D(out[iChk][1]);
														out[iChk][1] = new Vector3D(out[iChk][2]);
														out[iChk][2] = temp;
													}
												}
												break;
											// Else, if this list already has this point, skip out (to avoid doubling it).
											} else if (out[iChk][iChk2].getX() <= testV.getX() + pmprecision && out[iChk][iChk2].getX() >= testV.getX() - pmprecision && out[iChk][iChk2].getY() <= testV.getY() + pmprecision && out[iChk][iChk2].getY() >= testV.getY() - pmprecision && out[iChk][iChk2].getZ() <= testV.getZ() + pmprecision && out[iChk][iChk2].getZ() >= testV.getZ() - pmprecision) {
												break;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return out;
	}
	
	// SimpleCorrectPlanes(MAPBrush, float)
	// Uses all sides' defined points to ensure all planes are flipped correctly.
	public static MAPBrush SimpleCorrectPlanes(MAPBrush brush, double pmprecision) {
		MAPBrush newBrush=new MAPBrush(brush.getBrushnum(), brush.getEntnum(), 100, brush.isDetailBrush());
		Vector3D[] theVerts=new Vector3D[brush.getNumSides()*3];
		int index=0;
		for(int i=0;i<brush.getNumSides();i++) {
			if(brush.getSide(i).isDefinedByTriangle()) {
				theVerts[index++]=brush.getSide(i).getTriangle()[0];
				theVerts[index++]=brush.getSide(i).getTriangle()[1];
				theVerts[index++]=brush.getSide(i).getTriangle()[2];
			}
		}
		
		if(theVerts[0]==null) { // If this brush had no sides with vertices defined
			return null; // Return null. With any luck this will cause an exception. :troll:
		}
		
		for(int i=0;i<brush.getNumSides();i++) { // For each side of the brush
			MAPBrushSide currentSide=brush.getSide(i);
			if(!currentSide.isDefinedByTriangle()) { // If the side isn't THX certified
				int j=0;
				while(theVerts[j]!=null) { // For each good vertex in the sides
					if(currentSide.getPlane().distance(theVerts[j]) > pmprecision) {
						currentSide.flipPlane();
					}
					j++;
				}
			}
			newBrush.add(currentSide);
		}
		
		return newBrush;
	}
	
	// Use if brush has no triangles.
	/// Author:		UltimateSniper
	/// Returns:	Ordered list of normalised vertex triplets (ready to feed in to map).
	public static Vector3D[][] AdvancedCorrectPlanes(Plane[] allplanes, float pmprecision) throws java.lang.ArrayIndexOutOfBoundsException {
		// Method:
		//1. Collect all vertices created by plane intercepts.
		//2. Create arrays of these vertices and inputted planes, to access planes via points they intersect and vice versa.
		//  MORE IMPORTANTLY, create an array indicating sides of each plane each vertex is on (-1 for -, 0 for on, 1 for +).
		//3. Run through each possible cavity (each combination of sides of each plane), collecting satisfying vertices.
		//    Correct cavity is found when there are at least 3 vertices on each plane.
		//    If fail, returns Vector3D[0][].
		//4. Generate central point of brush, and use it to produce normalised vertex triplets to return.
		
		// 1. Collect all plane intersects (all possible corners).
		//Find MaxVerts: max = N!/3!(N-3)! = (1/3!) * (N/(N-3)) * ((N-1)/(N-4)) * ((N-2)/(N-5)) * ... * (5/2) * 4 * 3!
		double dmaxVerts = 4.0;
		for (int iP = allplanes.length; iP > 4; iP--) {
			dmaxVerts *= iP / (iP - 3.00);
		}
		Vector3D[] allverts = new Vector3D[(int)dmaxVerts]; // Max possible number of unique plane trisects: nC3.
		int iallverts = 0; // Pointer, so we know next vacant index.
		for (int iP1 = 0; iP1 < allplanes.length; iP1++) {
			for (int iP2 = iP1 + 1; iP2 < allplanes.length; iP2++) {
				for (int iP3 = iP2 + 1; iP3 < allplanes.length; iP3++) {
					Vector3D testV = allplanes[iP1].trisect(allplanes[iP2], allplanes[iP3]);
					if (!testV.equals(Vector3D.undefined)) {
						boolean hasVtx = false;
						for (int iVtx = 0; iVtx < iallverts; iVtx++) {
							if (allverts[iVtx].getX() + pmprecision > testV.getX() && allverts[iVtx].getX() - pmprecision < testV.getX() && allverts[iVtx].getY() + pmprecision > testV.getY() && allverts[iVtx].getY() - pmprecision < testV.getY() && allverts[iVtx].getZ() + pmprecision > testV.getZ() && allverts[iVtx].getZ() - pmprecision < testV.getZ()) {
								hasVtx = true;
								break;
							}
						}
						if (!hasVtx) {
							allverts[iallverts] = testV;
							iallverts++;
						}
					}
				}
			}
		}
		Vector3D[] tmp = new Vector3D[iallverts];
		System.arraycopy(allverts, 0, tmp, 0, iallverts);
		allverts = tmp;
		
		
		// 2. Make array to access verts' sides of planes (can also be used to check if vert is on plane).
		byte[][] VertPlaneSides = new byte[allverts.length][];
		for (int iV = 0; iV < allverts.length; iV++) {
			byte[] PlaneSides = new byte[allplanes.length];
			for (int iVP = 0; iVP < allplanes.length; iVP++) {
				double dist = allplanes[iVP].distance(allverts[iV]);
				if (dist < pmprecision && dist > -pmprecision) {
					PlaneSides[iVP] = 0;
				} else {
					PlaneSides[iVP] = ((dist >= pmprecision) ? (byte)1 : (byte)-1);
				}
			}
			VertPlaneSides[iV] = PlaneSides;
		}
		
		// THEORY: Collect vertices that are all either on, or on the same side of all planes.
		//         If there are at least 3 vertices on each plane, then this is the correct shape.
		//  NOTES: -Some vertices may be included in multiple collections.
		//         -Must have 3 vertices sharing 1 plane, and a fourth which is not on that plane to start 'cavity'.
		//         -4 defining vertices cannot make more than 1 'cavity'.
		
		
		// Java is retarded, as it doesn't allow uints. This causes me a serious problem with this, because I am not sure
		// whether or not bitwise operations will return positive values, or switch the signs, or switch the bit order...
		
		// Cannot handle more than the max positive val for a long, and need a bit to represent each plane.
		if (allplanes.length > 62) {
			throw new java.lang.ArrayIndexOutOfBoundsException("More than 62 planes in brush!");
		}
		
		
		// 3. Find all vertices which satisfy each possible cavity, and break when true brush is found.
		// Let the madness of this one great fucking for-loop commence...
		int[] TrueCorns = new int[0];
		
		for (long lCav = 0; lCav < (1 << allplanes.length); lCav++) {
			int[] Corns = new int[allverts.length];
			int iCorns = 0;
			for (int iCorn = 0; iCorn < allverts.length; iCorn++) {
				boolean addable = true;
				for (int iPlane = 0; iPlane < allplanes.length; iPlane++) {
					// Get bit value of lCav which represents this plane (true = +, false = -), check if vert is addable.
					if (((lCav >> iPlane) & 1) == 1) {
						if (VertPlaneSides[iCorn][iPlane] == -1) {
							addable = false;
						}
					} else {
						if (VertPlaneSides[iCorn][iPlane] == 1) {
							addable = false;
						}
					}
				}
				if (addable) {
					Corns[iCorns] = iCorn;
					iCorns++;
				}
			}
			// Check if we already have the brush...
			if (iCorns >= allplanes.length) {
				boolean isBrush = true;
				for (int iChkP = 0; iChkP < allplanes.length; iChkP++) {
					// If all planes have at least 3 verts in this solid, IT IS THE BRUSH.
					int numOnPlane = 0;
					for (int iChkC = 0; iChkC < iCorns; iChkC++) {
						if (VertPlaneSides[Corns[iChkC]][iChkP] == 0) {
							numOnPlane++;
							if (numOnPlane >= 3) {
								break;
							}
						}
					}
					if (numOnPlane < 3) {
						isBrush = false;
						break;
					}
				}
				if (isBrush) {
					// Copy to TrueCorns.
					TrueCorns = new int[iCorns];
					System.arraycopy(Corns, 0, TrueCorns, 0, iCorns);
					break;
				}
			}
		}
		
		
		
		
		
		// Idea: Loop all verts on 1 plane.
		//   Collect 1 cavity.
		//    Collect others, but make sure that points are not on same sides of planes as others.
		//    (Start plane side array (make upper-level one, too), and make sure that it doesn't become identical to any previous arrays.)
		
		
		
		
		

		/*Vector3D[] corners = new Vector3D[allverts.length];
		
		
		
		// SCREW IT. NEW PLAN...
		// Iterate through all possibilities of all sides of all planes.
		// NEED: byte[vert][plane] = -1, 0, +1, to tell whether or not vertex can be included in decompile.
		// 2^n iterations, sequence of booleans determines sides of planes.
		
		
		
		
		
		Vector3D[][] solidCollection = new Vector3D[allverts.length];
		int isolidCollection = 0;
		
		// METHOD 1: Take 1 starting vert, and find all verts on same side of all planes, and on same side of startplanes as first verts found not on startplanes.
		for (int iStart = 0; iStart < PlaneVerts[0].length; iStart++) {
			
			// Set up list of sides of planes which define this solid. 0=ON 1=+Norm -1=-Norm
			byte[] planeSidesStart = new byte[allplanes.length];
			// Collect info from first point.
			for (int i1Side = 1; i1Side < allplanes.length; i1Side++) {
				if (indexOf(PlaneVerts[i1Side], PlaneVerts[0][iStart]) == -1) {
					planeSidesStart[i1Side] = ((allplanes[i1Side].distance(PlaneVerts[0][iStart]) > 0) ? 1 : -1);
				} else {
					planeSidesStart[i1Side] = 0;
				}
			}
			
			// Collect a list of points which satisfy the conditions in planeSideStart.
			int[] potCorns = new int[allverts.length];
			int ipotCorns = 0;
			for (int iTCorn = 0; iTCorn < allverts.length; iTCorn++) {
				if (iTCorn != indexOf(allverts, PlaneVerts[0][iStart])) {
					// Check if on same side of planes as solid-defining list...
					boolean addable = true;
					for (int iPlanes = 0; iPlanes < allplanes.length; iPlanes++) {
						if (indexOf(PlaneVerts[iPlanes], allpoints[iCorn]) == -1 && planeSidesStart[iPlanes] != 0) {
							double dist = allplanes[iPlanes].distance(allpoints[iCorn]);
							if ((dist > 0 && planeSidesStart[iPlanes] < 0) || (dist < 0 && planeSidesStart[iPlanes] > 0)) {
								addable = false;
							}
						}
					}
					if (addable) {
						potCorns[ipotCorns] = iTCorn;
						ipotCorns++;
					}
				}
			}
			
			int[] basePlanes = new int[VertPlanes[indexOf(allverts, PlaneVerts[0][iStart])].length];
			// A possibility of 2^NumUnknownPlaneSides number of valid solids. Possibly on + or - side of each unknown plane.
			// Need to code something to run over each possibility once.
			for (int iSideCheck = 0; iSideCheck < basePlanes.length; iSideCheck++) {
				
			
			// For all possible solids starting from this point...
			for (int iChkNum = 0; iChkNum < allverts.length; iChkNum++) {
				// Look through all verts & find all on same side of planes.
				int[] blockcorners = new int[allpoints.length];
				blockcorners[0] = indexOf(allverts, PlaneVerts[0][iStart]);
				int iblockcorners = 1;

				// Set up list of sides of planes which define this solid. 0=ON 1=+Norm -1=-Norm
				// DEEP COPY IT PLEASE?
				byte[] planeSides = planeSidesStart;
				
				// Screw this complicated shit, doesn't work anyway.
				// INSTEAD, must find valid solid, and verts on opposite side of a plane that startVert is on than other solids.
				
				//
				

				int FirstID = 0;
				for (int iCorn = 0; iCorn < allverts.length; iCorn++) {
					boolean checkable = true;
					boolean isFirst = 
					// If is startvert or is contained in any solid also containing startvert, IS NOT CHECKABLE FIRST TIME AROUND.
					// Do for this vert, then reset to find all verts in solid.
					for (int iCheck = 0; iCheck < iSolidCollection; iCheck++) {
						
					if (!allpoints[iCorn].equals(PlaneVerts[0][iStart]) && iCorn != FirstID)
					if (!allpoints[iCorn].equals(PlaneVerts[0][iStart])) {
						// Check if on same side of planes as solid-defining list...
						int[] thisPlaneSides = new int[allplanes.length]
						int ithisPlaneSides = 0;
						boolean addable = true;
						for (int iPlanes = 0; iPlanes < allplanes.length; iPlanes++) {
							if (indexOf(PlaneVerts[iPlanes], allpoints[iCorn]) == -1) {
								double dist = allplanes[iPlanes].distance(allpoints[iCorn]);
								if (planeSides[iPlanes] == 0) {
									// If point is valid at end, temp set vert's planesides to +- plane index.
									thisPlaneSides[ithisPlaneSides] = ((dist > 0) ? iPlanes + 1 : -iPlanes - 1);
								} else if ((dist > 0 && planeSides[iPlanes] < 0) || (dist < 0 && planeSides[iPlanes] > 0)) {
									addable = false;
								}
							}
						}
						if (addable) {
							for (int iNewSides = 0; iNewSides < ithisPlaneSides; iNewSides++) {
								if (thisPlaneSides[iNewSides] > 0) {
									planeSides[thisPlaneSides[iNewSides] - 1] = 1;
								} else {
									planeSides[-thisPlaneSides[iNewSides] - 1] = -1;
								}
							}
							blockcorners[iblockcorners] = iCorn;
							iblockcorners++;
						}
					}
				}
				solidCollection[isolidCollection] = new Vector3D[iblockcorners];
				for (int iKnownCorn = 0; iKnownCorn < iblockcorners; iKnownCorn++) {
					solidCollection[isolidCollection][iKnownCorn] = allverts[blockcorners[iKnownCorn]];
				}
				isolidCollection++;
			}
		
		
		
		
		// For each vert on base plane, find 2 adjacent vertices.
		for (int iV1 = 0; iV1 < PlaneVerts[0].length; iV1++) {
			// For each other vert on base plane, check if 1st vert and this one share 2 planes & are on same sides of all planes.
			for (int iV2 = iV1 + 1; iV2 < PlaneVerts[0].length; iV2++) {
				boolean isOKVert = false;
				// Check if these 2 share 2 planes.
				// For each plane on first vert...
				for (int iChkP = 0; iChkP < VertPlanes[indexOf(allverts, PlaneVerts[0][iV1])].length; iChkP++)
					// If plane is NOT base-plane...
					if (!allplanes[0].getNormal().equals(VertPlanes[indexOf(allverts, PlaneVerts[0][iV1])][iChkP]) || allplanes[0].getDist() != VertPlanes[indexOf(allverts, PlaneVerts[0][iV1])][iChkP].getDist())
						// If this plane is common, VERT IS OK...
						if (indexOf(VertPlanes[indexOf(allverts, PlaneVerts[0][iV2])], VertPlanes[indexOf(allverts, PlaneVerts[0][iV1])][iChkP]) > -1)
							isOKVert = true;
				// Check if these 2 are on same side of all planes they are not on.
				if (isOKVert) {
					isOKVert = false;
					// For each plane, not base-plane...
					for (int iChkP2 = 1; iChkP2 < allplanes.length; iChkP2++)
						// If V1 and V2 are NOT on plane...
						if (indexOf(PlaneVerts[iChkP2], PlaneVerts[0][iV1]) == -1 && indexOf(PlaneVerts[iChkP2], PlaneVerts[0][iV2]) == -1) {
							// If V1 and V2 are on same side of plane...
							double dv1 = allplanes[iChkP2].distance(PlaneVerts[0][iV1]);
							double dv2 = allplanes[iChkP2].distance(PlaneVerts[0][iV2]);
							if ((dv1 > 0 && dv2 > 0) || (dv1 < 0 && dv2 < 0))
								isOKVert = true;
						}
				}
				if (isOKVert) {
					for (int iV3 = iV2 + 1; iV3 < PlaneVerts[0].length; iV3++) {
						
		*/
		
		
		
		// Return null value if method failed.
		if (TrueCorns.length == 0) {
			return new Vector3D[0][];
		}
		
		
		// 4. Create brush central point, and use it to create normalised plane triplets.
		// Create central point of brush for normalising vert-planes.
		Vector3D centrePoint = new Vector3D(0.0, 0.0, 0.0);
		for (int iCorn = 0; iCorn < TrueCorns.length; iCorn++) {
			centrePoint = centrePoint.add(allverts[TrueCorns[iCorn]]);
		}
		centrePoint = centrePoint.scale(1.0 / (double)TrueCorns.length);
		// Use corners to generate brush plane triplets.
		Vector3D[][] output = new Vector3D[allplanes.length][];
		for (int iPlane = 0; iPlane < allplanes.length; iPlane++) {
			int[] vertPlane = new int[3];
			int ivertPlane = 0;
			for (int iCorn = 0; iCorn < TrueCorns.length; iCorn++) {
				if (VertPlaneSides[TrueCorns[iCorn]][iPlane] == 0) {
					vertPlane[ivertPlane] = TrueCorns[iCorn];
					ivertPlane++;
					if (ivertPlane == 3) {
						break;
					}
				}
			}
			// Order triplet correctly & save to output array.
			if (new Plane(allverts[vertPlane[0]], allverts[vertPlane[1]], allverts[vertPlane[2]]).distance(centrePoint) > 0) {
				output[iPlane] = new Vector3D[] { allverts[vertPlane[0]] , allverts[vertPlane[2]] , allverts[vertPlane[1]] };
			} else {
				output[iPlane] = new Vector3D[] { allverts[vertPlane[0]] , allverts[vertPlane[1]] , allverts[vertPlane[2]] };
			}
		}
		return output;
	}
	
	// Some algorithms might produce planes which are correctly normalized, but
	// some don't actually contribute to the solid (such as those solids created
	// by iterating through a binary tree, and keeping track of all node subdivisions).
	public static MAPBrush cullUnusedPlanes(MAPBrush in, float precision) {
		Plane[] thePlanes=in.getPlanes();
		
		// Step 1: Get all available vertices
		double numVerts = 4;
		// Iterative nCr algorithm; thanks to the code above
		for(int i=thePlanes.length;i>4;i--) {
			numVerts *= (double)i/(double)(i-3);
		}
		Vector3D[] theVerts = new Vector3D[(int)Math.round(numVerts)];
		int index=0;
		for(int i=0;i<thePlanes.length-2;i++) {
			for(int j=i+1;j<thePlanes.length-1;j++) {
				for(int k=j+1;k<thePlanes.length;k++) {
					theVerts[index++]=thePlanes[i].trisect(thePlanes[j], thePlanes[k]);
				}
			}
		}
	
		// Step 2: Throw out all vertices on the wrong side of any plane, since they're
		// all facing the "right" way.
		for(int i=0;i<theVerts.length;i++) {
			for(int j=0;j<thePlanes.length;j++) {
				if(thePlanes[j].distance(theVerts[i]) > precision) {
					theVerts[i]=Vector3D.undefined;
					break; //break the inner loop, let the outer loop iterate
				}
			}
		}
		
		// Step 3: Only keep sides which have three or more vertices defined
		int side=0;
		for(int i=0;i<thePlanes.length;i++) {
			int numMatches=0;
			for(int j=0;j<theVerts.length;j++) {
				if(Math.abs(thePlanes[i].distance(theVerts[j])) < precision) {
					numMatches++;
				}
				if(numMatches>=3) {
					break;
				}
			}
			if(numMatches<3) {
				in.delete(side); // Delete this side from the brush
				side--;
			}
			side++;
		}
		return in;
	}

	public static Vector3D[] extrapPlanePoints(Plane in, double planePointCoef) {
		Vector3D[] plane=new Vector3D[3];
		// Figure out if the plane is parallel to two of the axes. If so it can be reproduced easily
		if(in.getB()==0 && in.getC()==0) { // parallel to plane YZ
			plane[0]=new Vector3D(in.getDist()/in.getA(), -planePointCoef, planePointCoef);
			plane[1]=new Vector3D(in.getDist()/in.getA(), 0, 0);
			plane[2]=new Vector3D(in.getDist()/in.getA(), planePointCoef, planePointCoef);
			if(in.getA()>0) {
				plane=Plane.flip(plane);
			}
		} else {
			if(in.getA()==0 && in.getC()==0) { // parallel to plane XZ
				plane[0]=new Vector3D(planePointCoef, in.getDist()/in.getB(), -planePointCoef);
				plane[1]=new Vector3D(0, in.getDist()/in.getB(), 0);
				plane[2]=new Vector3D(planePointCoef, in.getDist()/in.getB(), planePointCoef);
				if(in.getB()>0) {
					plane=Plane.flip(plane);
				}
			} else {
				if(in.getA()==0 && in.getB()==0) { // parallel to plane XY
					plane[0]=new Vector3D(-planePointCoef, planePointCoef, in.getDist()/in.getC());
					plane[1]=new Vector3D(0, 0, in.getDist()/in.getC());
					plane[2]=new Vector3D(planePointCoef, planePointCoef, in.getDist()/in.getC());
					if(in.getC()>0) {
						plane=Plane.flip(plane);
					}
				} else { // If you reach this point the plane is not parallel to any two-axis plane.
					if(in.getA()==0) { // parallel to X axis
						plane[0]=new Vector3D(-planePointCoef, planePointCoef*planePointCoef, -(planePointCoef*planePointCoef*in.getB()-in.getDist())/in.getC());
						plane[1]=new Vector3D(0, 0, in.getDist()/in.getC());
						plane[2]=new Vector3D(planePointCoef, planePointCoef*planePointCoef, -(planePointCoef*planePointCoef*in.getB()-in.getDist())/in.getC());
						if(in.getC()>0) {
							plane=Plane.flip(plane);
						}
					} else {
						if(in.getB()==0) { // parallel to Y axis
							plane[0]=new Vector3D(-(planePointCoef*planePointCoef*in.getC()-in.getDist())/in.getA(), -planePointCoef, planePointCoef*planePointCoef);
							plane[1]=new Vector3D(in.getDist()/in.getA(), 0, 0);
							plane[2]=new Vector3D(-(planePointCoef*planePointCoef*in.getC()-in.getDist())/in.getA(), planePointCoef, planePointCoef*planePointCoef);
							if(in.getA()>0) {
								plane=Plane.flip(plane);
							}
						} else {
							if(in.getC()==0) { // parallel to Z axis
								plane[0]=new Vector3D(planePointCoef*planePointCoef, -(planePointCoef*planePointCoef*in.getA()-in.getDist())/in.getB(), -planePointCoef);
								plane[1]=new Vector3D(0, in.getDist()/in.getB(), 0);
								plane[2]=new Vector3D(planePointCoef*planePointCoef, -(planePointCoef*planePointCoef*in.getA()-in.getDist())/in.getB(), planePointCoef);
								if(in.getB()>0) {
									plane=Plane.flip(plane);
								}
							} else { // If you reach this point the plane is not parallel to any axis. Therefore, any two coordinates will give a third.
								plane[0]=new Vector3D(-planePointCoef, planePointCoef*planePointCoef, -(-planePointCoef*in.getA()+planePointCoef*planePointCoef*in.getB()-in.getDist())/in.getC());
								plane[1]=new Vector3D(0, 0, in.getDist()/in.getC());
								plane[2]=new Vector3D(planePointCoef, planePointCoef*planePointCoef, -(planePointCoef*in.getA()+planePointCoef*planePointCoef*in.getB()-in.getDist())/in.getC());
								if(in.getC()>0) {
									plane=Plane.flip(plane);
								}
							}
						}
					}
				}
			}
		}
		return plane;
	}
}
		