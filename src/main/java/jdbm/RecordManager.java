package jdbm;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 *  An interface/abstract class to manage records, which are objects serialized to byte[] on background.
 *  <p>
 *  The set of record operations is simple: fetch, insert, update and delete.
 *  Each record is identified using a "rowid" and contains a byte[] data block serialized to object.
 *  Rowids are returned on inserts and you can store them someplace safe
 *  to be able to get  back to them.  Data blocks can be as long as you wish,
 *  and may have lengths different from the original when updating.
 *  <p>
 *  RecordManager is responsible for handling transactions.
 *  JDBM2 supports only single transaction for data store.
 *  See <code>commit</code> and <code>roolback</code> methods for more details.
 *  <p>
 *  RecordManager is also factory for primary Maps.
 *  <p>
 * @author Jan Kotek
 * @author Alex Boisvert
 * @author Cees de Groot
 */
public interface RecordManager {
    /**
     *  Inserts a new record using standard java object serialization.
     *
     *  @param obj the object for the new record.
     *  @return the rowid for the new record.
     *  @throws java.io.IOException when one of the underlying I/O operations fails.
     */
    long insert(Object obj) throws IOException;

    /**
     *  Inserts a new record using a custom serializer.
     *
     *  @param obj the object for the new record.
     *  @param serializer a custom serializer
     *  @return the rowid for the new record.
     *  @throws java.io.IOException when one of the underlying I/O operations fails.
     */
    <A> long insert(A obj, Serializer<A> serializer) throws IOException;

    /**
     *  Deletes a record.
     *
     *  @param recid the rowid for the record that should be deleted.
     *  @throws java.io.IOException when one of the underlying I/O operations fails.
     */
    void delete(long recid) throws IOException;

    /**
     *  Updates a record using standard java object serialization.
     *
     *  @param recid the recid for the record that is to be updated.
     *  @param obj the new object for the record.
     *  @throws java.io.IOException when one of the underlying I/O operations fails or given recid does not exists.
     */
    void update(long recid, Object obj) throws IOException;

    /**
     *  Updates a record using a custom serializer.
     *  If given recid does not exist, IOException will be thrown before/during commit (cache).
     *
     *
     *  @param recid the recid for the record that is to be updated.
     *  @param obj the new object for the record.
     *  @param serializer a custom serializer
     *  @throws java.io.IOException when one of the underlying I/O operations fails
     */
    <A> void update(long recid, A obj, Serializer<A> serializer)
        throws IOException;

    /**
     *  Fetches a record using standard java object serialization.
     *  If given recid does not exist, IOException will be thrown before/during commit (cache).
     *
     *  @param recid the recid for the record that must be fetched.
     *  @return the object contained in the record, null if given recid does not exist
     *  @throws java.io.IOException when one of the underlying I/O operations fails.
     */
    <A> A fetch(long recid) throws IOException;

    /**
     *  Fetches a record using a custom serializer.
     *
     *  @param recid the recid for the record that must be fetched.
     *  @param serializer a custom serializer
     *  @return the object contained in the record, null if given recid does not exist
     *  @throws java.io.IOException when one of the underlying I/O operations fails.
     */
    <A> A fetch(long recid, Serializer<A> serializer)
        throws IOException;

    /**
     *  Fetches a record using a custom serializer and optionaly disabled cache
     *
     *  @param recid the recid for the record that must be fetched.
     *  @param serializer a custom serializer
     *  @param disableCache true to disable any caching mechanism
     *  @return the object contained in the record, null if given recid does not exist
     *  @throws java.io.IOException when one of the underlying I/O operations fails.
     */
    <A> A fetch(long recid, Serializer<A> serializer, boolean disableCache)
        throws IOException;

    /**
     *  Closes the record manager and release resources.
     *  Record manager can not be used after it was closed
     *
     *  @throws java.io.IOException when one of the underlying I/O operations fails.
     */
    void close()
        throws IOException;

    /**
     * Empty cache. This may be usefull if you need to release memory.
     *
     * @throws java.io.IOException
     */
    void clearCache() throws IOException;

    /**
     * Defragments storage, so it consumes less space.
     * This commits any uncommited data.
     *
     * @throws java.io.IOException
     */
    void defrag() throws IOException;

    /**
     * Commit (make persistent) all changes since beginning of transaction.
     * JDBM supports only single transaction.
     */
    void commit()
        throws IOException;

    /**
     * This calculates some database statistics.
     * Mostly what collections are presents and how much space is used.
     * @return statistics contained in string
     */
    String calculateStatistics();

    /**
     * Rollback (cancel) all changes since beginning of transaction.
     * JDBM supports only single transaction.
     * This operations affects all maps created by this RecordManager.
     */
    void rollback()
        throws IOException;


    <K, V> PrimaryHashMap<K, V> loadHashMap(String name);

    /**
     * Creates or load existing Primary Hash Map which persists data into DB.
     *
     *
     * @param <K> Key type
     * @param <V> Value type
     * @param name record name
     * @return
     */
    <K, V> PrimaryHashMap<K, V> createHashMap(String name);


    /**
     * Creates or load existing Primary Hash Map which persists data into DB.
     * Map will use custom serializers for Keys and Values.
     * Leave keySerializer null to use default serializer for keys
     *
     * @param <K> Key type
     * @param <V> Value type
     * @param name record name
     * @param keySerializer serializer to be used for Keys, leave null to use default serializer
     * @param valueSerializer serializer to be used for Values
     * @return
     */
    <K, V> PrimaryHashMap<K, V> createHashMap(String name, Serializer<K> keySerializer, Serializer<V> valueSerializer);

    <K> Set<K> createHashSet(String name);

    <K> Set<K> loadHashSet(String name);

    <K> Set<K> createHashSet(String name, Serializer<K> keySerializer);

    <K, V> PrimaryTreeMap<K, V> loadTreeMap(String name);

    /**
     * Creates or load existing Primary TreeMap which persists data into DB.
     *
     *
     * @param <K> Key type
     * @param <V> Value type
     * @param name record name
     * @return
     */
    <K extends Comparable, V> PrimaryTreeMap<K, V> createTreeMap(String name);

    /**
     * Creates or load existing TreeMap which persists data into DB.
     *
     * @param <K> Key type
     * @param <V> Value type
     * @param name record name
     * @param keyComparator Comparator used to sort keys
     * @param keySerializer Serializer used for keys. This may reduce disk space usage     *
     * @param valueSerializer Serializer used for values. This may reduce disk space usage
     * @return
     */
    <K, V> PrimaryTreeMap<K, V> createTreeMap(String name,
                                              Comparator<K> keyComparator, Serializer<K> keySerializer,Serializer<V> valueSerializer);

    <K> SortedSet<K> loadTreeSet(String name);

    <K> SortedSet<K> createTreeSet(String name);

    <K> SortedSet<K> createTreeSet(String name, Comparator<K> keyComparator, Serializer<K> keySerializer);

    <K> List<K> createLinkedList(String name);

    <K> List<K> createLinkedList(String name, Serializer<K> serializer);

    <K> List<K> loadLinkedList(String name);


    /**
     * Copy data from RecordManager into zip db.
     * @param zipFile
     */
    void copyToZipStore(String zipFile);
}
