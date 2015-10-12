package com.github.jiloc.USTweetsAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

//import org.geotools.data.CachingFeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Prompts the user for a shapefile and displays the contents on the screen in a map frame.
 * <p>
 * This is the GeoTools Quickstart application used in documentationa and tutorials. *
 */
public class Geolocalizator {
	private FileDataStore store;
	private SimpleFeatureSource featureSource;
	// private CachingFeatureSource cachedFeatureSource;
	// private GeometryDescriptor geometryDesc;
	// private CoordinateReferenceSystem targetCRS;
	// private String geometryPropertyName;
		
	public Geolocalizator(String filePath){

        File file = new File(filePath);
        try {
			this.store = FileDataStoreFinder.getDataStore(file);
			this.featureSource = store.getFeatureSource();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     
        //CachingFeatureSource is deprecated as experimental (not yet production ready)
		// try {
		// 	this.cachedFeatureSource = new CachingFeatureSource(store.getFeatureSource());
		// } catch (IOException e) {
		// 	// TODO Auto-generated catch block
		// 	e.printStackTrace();
		// }
		// this.cachedFeatureSource.
        // this.geometryDesc = featureSource.getSchema().getGeometryDescriptor();
        // this.targetCRS = geometryDesc.getCoordinateReferenceSystem();
        // this.geometryPropertyName = geometryDesc.getLocalName();        
	}
	
	public Point getPointFromCoordinates(double longitude, double latitude) {
	    GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
	    Coordinate coord = new Coordinate(longitude, latitude);
	    return geometryFactory.createPoint(coord);
	}
	
	public ArrayList<String> getStateFromCoordinates(double longitude, double latitude) {
		
        Point position = this.getPointFromCoordinates(longitude, latitude);

        Filter pointInPolygon = null;
		try { 
			pointInPolygon = CQL.toFilter(
			    "CONTAINS(the_geom, POINT(" + position.getX() + " " + position.getY() + "))");
		} catch (CQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        SimpleFeatureCollection features = null;
		try {
			features = this.featureSource.getFeatures(pointInPolygon);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        if (features.isEmpty()){
        	return null;
        }
        ArrayList<String> states = new ArrayList<String>();
        SimpleFeatureIterator iterator = features.features();
        
        try {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                states.add((String) feature.getAttribute("NAME"));
            }
        } finally {
            iterator.close();
        }
        return states;
	}
		
    public static void main(String[] args) throws Exception {
        // display a data store file chooser dialog for shapefiles
		Geolocalizator localizator = new Geolocalizator(
			"src/main/resources/tl_2014_us_state/tl_2014_us_state.shp");
		System.out.println(localizator.getStateFromCoordinates(-95.7335419655, 38.8612531494)); // Kansas
		System.out.println(localizator.getStateFromCoordinates(41.902783, 12.496366));  // Rome
    }

}