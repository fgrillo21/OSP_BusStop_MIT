package busstop.customtrip.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.opentripplanner.api.model.Itinerary;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nice.fontaine.overpass.models.response.geometries.Bounds;
import nice.fontaine.overpass.models.response.geometries.Coordinate;
import nice.fontaine.overpass.models.response.geometries.Element;
import nice.fontaine.overpass.models.response.geometries.Geometry;
import nice.fontaine.overpass.models.response.geometries.Node;
import nice.fontaine.overpass.models.response.geometries.Relation;
import nice.fontaine.overpass.models.response.geometries.Way;
import nice.fontaine.overpass.models.response.geometries.members.Member;

public class EnrichedItinerary  implements Serializable {
    private static final long serialVersionUID = 3984973894179167604L;
    private Itinerary     itinerary;
    private String name;
    private transient List<LatLng>  itineraryDecoded;
    private FeaturesCount historicCount;
    private FeaturesCount greenCount;
    private FeaturesCount panoramicCount;
    private Element[]     historicalFeatures;
    private Element[]     greenFeatures;
    private Element[]     panoramicFeatures;
    private int           transfersCount;

    public static EnrichedItineraryBuilder newEnrichedItinerary() {
        return new EnrichedItineraryBuilder();
    }

    private EnrichedItinerary(Itinerary itinerary, List<LatLng>  itineraryDecoded, FeaturesCount historicCount, FeaturesCount greenCount, FeaturesCount panoramicCount, Element[] historicalFeatures, Element[] greenFeatures, Element[] panoramicFeatures, String name, int transfersCount) {
        this.itinerary          = itinerary;
        this.itineraryDecoded   = itineraryDecoded;
        this.historicCount      = historicCount;
        this.greenCount         = greenCount;
        this.panoramicCount     = panoramicCount;
        this.historicalFeatures = historicalFeatures;
        this.greenFeatures      = greenFeatures;
        this.panoramicFeatures  = panoramicFeatures;
        this.name               = name;
        this.transfersCount     = transfersCount;
    }

    private void serializeElement(final ObjectOutputStream out, Element feature) throws IOException {

        if (feature.type.equalsIgnoreCase("node")) {

            Node node = (Node) feature;

            // In Element
            out.writeUTF(feature.type);
            out.writeObject(feature.tags);

            // In Geometry
            out.writeLong(node.id);
            out.writeObject(node.timestamp);
            out.writeInt(node.version);
            out.writeLong(node.changeset);

            String str = node.user;
            if (node.user == null)
                str = "";

            out.writeUTF(str);

            out.writeLong(node.uid);

            // In Node
            out.writeDouble(node.lat);
            out.writeDouble(node.lon);
        }
        else if (feature.type.equalsIgnoreCase("way")) {

            Way way = (Way) feature;

            // In Element
            out.writeUTF(feature.type);
            out.writeObject(feature.tags);

            // In Geometry
            out.writeLong(way.id);
            out.writeObject(way.timestamp);
            out.writeInt(way.version);
            out.writeLong(way.changeset);

            String str = way.user;
            if (way.user == null)
                str = "";
            out.writeUTF(str);
            out.writeLong(way.uid);

            // In Geometry2D
            out.writeDouble(way.bounds.minlat);
            out.writeDouble(way.bounds.minlon);
            out.writeDouble(way.bounds.maxlat);
            out.writeDouble(way.bounds.maxlon);

            if (way.center == null) {
                out.writeDouble(-32000);
                out.writeDouble(-32000);
            }
            else {
                out.writeDouble(way.center.lat);
                out.writeDouble(way.center.lon);
            }

            // In way
            out.writeObject(way.nodes);

            out.writeInt(way.geometry.length);

            for (int j = 0; j < way.geometry.length; ++j) {
                out.writeDouble(way.geometry[j].lat);
                out.writeDouble(way.geometry[j].lon);
            }
        }
        else {

            Relation relation = (Relation) feature;

            // In Element
            out.writeUTF(feature.type);
            out.writeObject(feature.tags);

            // In Geometry
            out.writeLong(relation.id);
            out.writeObject(relation.timestamp);
            out.writeInt(relation.version);
            out.writeLong(relation.changeset);

            String str = relation.user;
            if (relation.user == null)
                str = "";
            out.writeUTF(str);

            out.writeLong(relation.uid);

            // In Geometry2D
            out.writeDouble(relation.bounds.minlat);
            out.writeDouble(relation.bounds.minlon);
            out.writeDouble(relation.bounds.maxlat);
            out.writeDouble(relation.bounds.maxlon);
            if (relation.center == null) {
                out.writeDouble(-32000);
                out.writeDouble(-32000);
            }
            else {
                out.writeDouble(relation.center.lat);
                out.writeDouble(relation.center.lon);
            }

            // In relation
            out.writeInt(relation.members.length);

            for (int j = 0; j < relation.members.length; ++j) {

                Member member = relation.members[j];

                out.writeUTF(member.type);
                out.writeLong(member.ref);
                str = member.role;
                if (member.role == null)
                    str = "";
                out.writeUTF(str);
                out.writeDouble(member.lat);
                out.writeDouble(member.lon);

                out.writeInt(member.geometry.size());

                for (int k = 0; k < member.geometry.size(); ++k) {
                    out.writeDouble(member.geometry.get(k).lat);
                    out.writeDouble(member.geometry.get(k).lon);
                }
            }
        }
    }

    private Element deserializeElement(final ObjectInputStream in) throws IOException, ClassNotFoundException {

        // In Element
        String type;
        Map<String, String> tags;

        // In Node
        double lat;
        double lon;

        // In Geometry
        long id;
        Date timestamp;
        int version;
        long changeset;
        String user;
        long uid;

        // In Geometry2D
        double minLat, maxLat, minLng, maxLng;
        Bounds bounds = null;
        double centerLat, centerLng;

        // In Way
        long[] nodes;
        Coordinate[] geometry;

        // In Relation
        Member[] members;

        type = in.readUTF();

        if (type.equalsIgnoreCase("node")) {

            // In Element
            tags = (Map<String, String>) in.readObject();

            // In Geometry
            id = in.readLong();
            timestamp = (Date) in.readObject();
            version = in.readInt();
            changeset = in.readLong();

            user = in.readUTF();
            if (user.equals(""))
                user = null;
            uid = in.readLong();

            // In Node
            lat = in.readDouble();
            lon = in.readDouble();

            Constructor<Node> ctorNode = null;
            try {
                ctorNode = Node.class.getDeclaredConstructor();

                ctorNode.setAccessible(true);

                Node node = ctorNode.newInstance(tags, id, timestamp, version, changeset, user, uid, lat, lon);

                return node;

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else if (type.equalsIgnoreCase("way")) {

            // In Element
            tags = (Map<String, String>) in.readObject();

            // In Geometry
            id = in.readLong();
            timestamp = (Date) in.readObject();
            version = in.readInt();
            changeset = in.readLong();
            user = in.readUTF();
            if (user.equals(""))
                user = null;
            uid = in.readLong();

            // In Geometry2D
            minLat = in.readDouble();
            minLng = in.readDouble();
            maxLat = in.readDouble();
            maxLng = in.readDouble();

            Constructor<Bounds> constructor = null;
            try {
                constructor = Bounds.class.getDeclaredConstructor();

                constructor.setAccessible(true);

                bounds = constructor.newInstance(minLat, minLng, maxLat, maxLng);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            centerLat = in.readDouble();
            centerLng = in.readDouble();
            Coordinate center;
            if (centerLat == -32000 && centerLng == -32000)
                center = null;
            else
                center = new Coordinate(centerLat, centerLng);

            // In way
            nodes = (long[]) in.readObject();

            int nItems = in.readInt();
            geometry = new Coordinate[nItems];

            for (int j = 0; j < nItems; ++j) {
                double latJ = in.readDouble();
                double lngJ = in.readDouble();
                geometry[j] = new Coordinate(latJ, lngJ);
            }

            Constructor<Way> ctorWay = null;
            try {
                ctorWay = Way.class.getDeclaredConstructor();

                ctorWay.setAccessible(true);

                Way way = ctorWay.newInstance(tags, id, timestamp, version, changeset, user, uid, bounds, center, nodes, geometry);

                return way;

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        } else {

            // In Element
            tags = (Map<String, String>) in.readObject();

            // In Geometry
            id = in.readLong();
            timestamp = (Date) in.readObject();
            version = in.readInt();
            changeset = in.readLong();
            user = in.readUTF();
            if (user.equals(""))
                user = null;
            uid = in.readLong();

            // In Geometry2D
            minLat = in.readDouble();
            minLng = in.readDouble();
            maxLat = in.readDouble();
            maxLng = in.readDouble();

            Constructor<Bounds> constructor = null;
            try {
                constructor = Bounds.class.getDeclaredConstructor();

                constructor.setAccessible(true);

                bounds = constructor.newInstance(minLat, minLng, maxLat, maxLng);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            centerLat = in.readDouble();
            centerLng = in.readDouble();

            Coordinate center;
            if (centerLat == -32000 && centerLng == -32000)
                center = null;
            else
                center = new Coordinate(centerLat, centerLng);

            // In relation
            int nItems = in.readInt();
            members = new Member[nItems];

            for (int j = 0; j < nItems; ++j) {

                String memberType = in.readUTF();
                long ref = in.readLong();
                String role = in.readUTF();
                if (role.equals(""))
                    role = null;
                double memberLat = in.readDouble();
                double memberLng = in.readDouble();

                int size = in.readInt();

                List<Coordinate> memberGeometry = new ArrayList<Coordinate>();

                for (int k = 0; k < size; ++k) {
                    double cLat = in.readDouble();
                    double cLng = in.readDouble();
                    memberGeometry.add(new Coordinate(cLat, cLng));
                }

                Constructor<Member> ctorMember = null;
                try {
                    ctorMember = Member.class.getDeclaredConstructor();

                    ctorMember.setAccessible(true);

                    members[j] = ctorMember.newInstance(memberType, ref, role, memberLat, memberLng, memberGeometry);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            Constructor<Relation> ctorRelation = null;
            try {
                ctorRelation = Relation.class.getDeclaredConstructor();

                ctorRelation.setAccessible(true);

                Relation relation = ctorRelation.newInstance(tags, id, timestamp, version, changeset, user, uid, bounds, center, members);

                return relation;

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Serialize this instance.
     *
     * @param out Target to which this instance is written.
     * @throws IOException Thrown if exception occurs during serialization.
     */
    private void writeObject(final ObjectOutputStream out) throws IOException
    {
        Log.d("TRQ", "Serializzo");
        
        out.writeObject(this.itinerary);
        out.writeObject(this.historicCount);
        out.writeObject(this.greenCount);
        out.writeObject(this.panoramicCount);
        out.writeUTF(this.name);

        out.writeInt(historicalFeatures.length);

        for (int i = 0; i < historicalFeatures.length; ++i) {

            Element feature = historicalFeatures[i];
            serializeElement(out, feature);
        }

        out.writeInt(greenFeatures.length);

        for (int i = 0; i < greenFeatures.length; ++i) {

            Element feature = greenFeatures[i];
            serializeElement(out, feature);
        }

        out.writeInt(panoramicFeatures.length);

        for (int i = 0; i < panoramicFeatures.length; ++i) {

            Element feature = panoramicFeatures[i];
            serializeElement(out, feature);
        }
    }

    /**
     * Deserialize this instance from input stream.
     *
     * @param in Input Stream from which this instance is to be deserialized.
     * @throws IOException Thrown if error occurs in deserialization.
     * @throws ClassNotFoundException Thrown if expected class is not found.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        int length;

        Log.d("TRQ", "Deserializzo");

        this.itinerary      = (Itinerary) in.readObject();
        this.historicCount  = (FeaturesCount) in.readObject();
        this.greenCount     = (FeaturesCount) in.readObject();
        this.panoramicCount = (FeaturesCount) in.readObject();
        this.name           = in.readUTF();

        length = in.readInt();
        Element[] deserializedHistoricalFeatures = new Element[length];

        for (int i = 0; i < length; ++i) {
            deserializedHistoricalFeatures[i] = deserializeElement(in);
        }

        historicalFeatures = deserializedHistoricalFeatures;

        length = in.readInt();
        Element[] deserializedGreenFeatures = new Element[length];

        for (int i = 0; i < length; ++i) {
            deserializedHistoricalFeatures[i] = deserializeElement(in);
        }

        greenFeatures = deserializedGreenFeatures;

        length = in.readInt();
        Element[] deserializedPanoramicFeatures = new Element[length];

        for (int i = 0; i < length; ++i) {
            deserializedHistoricalFeatures[i] = deserializeElement(in);
        }

        panoramicFeatures = deserializedPanoramicFeatures;
    }

    private void readObjectNoData() throws ObjectStreamException
    {
        throw new InvalidObjectException("Stream data required");
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

    public void setItinerary(Itinerary itinerary) {this.itinerary = itinerary; }

    public List<LatLng> getItineraryDecoded() {
        return itineraryDecoded;
    }

    public FeaturesCount getHistoricCount() {
        return historicCount;
    }

    public int getHistoricAggregatedCount() {
        return historicCount.aggregatedCount();
    }

    public FeaturesCount getGreenCount() {
        return greenCount;
    }

    public int getGreenAggregatedCount() {
        return greenCount.aggregatedCount();
    }

    public FeaturesCount getPanoramicCount() {
        return panoramicCount;
    }

    public int getPanoramicAggregatedCount() {
        return panoramicCount.aggregatedCount();
    }

    public Element[] getHistoricalFeatures() {
        return historicalFeatures;
    }

    public Element[] getGreenFeatures() { return greenFeatures; }

    public Element[] getPanoramicFeatures() { return panoramicFeatures; }

    public String getName() { return name; }

    public int getTransfersCount() {
        return transfersCount;
    }

    public void setTransfersCount(int transfersCount) {
        this.transfersCount = transfersCount;
    }

    public static final class EnrichedItineraryBuilder {
        private Itinerary     itinerary;
        private List<LatLng>  itineraryDecoded;
        private FeaturesCount historicCount;
        private FeaturesCount greenCount;
        private FeaturesCount panoramicCount;
        private Element[]     historicalFeatures;
        private Element[]     greenFeatures;
        private Element[]     panoramicFeatures;
        private String        name;

        private int           transfersCount;

        public EnrichedItineraryBuilder withItinerary(Itinerary itinerary) {
            this.itinerary = itinerary;
            return this;
        }

        public EnrichedItineraryBuilder withItineraryDecoded(List<LatLng> itineraryDecoded) {
            this.itineraryDecoded = itineraryDecoded;
            return this;
        }

        public EnrichedItineraryBuilder withHistoricCount(FeaturesCount historicCount) {
            this.historicCount = historicCount;
            return this;
        }

        public EnrichedItineraryBuilder withGreenCount(FeaturesCount greenCount) {
            this.greenCount = greenCount;
            return this;
        }

        public EnrichedItineraryBuilder withPanoramicCount(FeaturesCount panoramicCount) {
            this.panoramicCount = panoramicCount;
            return this;
        }

        public EnrichedItineraryBuilder withHistoricalFeatures(Element[] elements) {
            this.historicalFeatures = elements;
            return this;
        }

        public EnrichedItineraryBuilder withGreenFeatures(Element[] elements) {
            this.greenFeatures = elements;
            return this;
        }

        public EnrichedItineraryBuilder withPanoramicFeatures(Element[] elements) {
            this.panoramicFeatures = elements;
            return this;
        }

        public EnrichedItinerary build() {
            return new EnrichedItinerary(itinerary, itineraryDecoded, historicCount, greenCount, panoramicCount, historicalFeatures, greenFeatures, panoramicFeatures, name, transfersCount);
        }

        public EnrichedItineraryBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public EnrichedItineraryBuilder withTransfersCount(int transfersCount) {
            this.transfersCount = transfersCount;
            return this;
        }
    }

    @Override
    public String toString() {
        return "EnrichedItinerary{" +
                "itinerary=" + itinerary +
                ", itineraryDecoded=" + itineraryDecoded +
                ", historicCount=" + historicCount +
                ", greenCount=" + greenCount +
                ", panoramicCount=" + panoramicCount +
                ", historicalElements=" + Arrays.toString(historicalFeatures) +
                ", greenElements=" + Arrays.toString(greenFeatures) +
                ", panoramicElements=" + Arrays.toString(panoramicFeatures) +
                ", name='" + name + '\'' +
                ", transfers=" + transfersCount +
                '}';
    }
}
