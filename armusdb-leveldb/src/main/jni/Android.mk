LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := leveldbjni
#LOCAL_SHARED_LIBRARIES := leveldb
LOCAL_C_INCLUDES := $(LOCAL_PATH)/leveldb-ndk/include
LOCAL_CPP_EXTENSION := .cc
LOCAL_CFLAGS := -DLEVELDB_PLATFORM_ANDROID -std=gnu++0x
LOCAL_SRC_FILES := com_ramseyboy_armusdb_DB.cc com_ramseyboy_armusdb_Iterator.cc com_ramseyboy_armusdb_WriteBatch.cc leveldbjni.cc
LOCAL_STATIC_LIBRARIES +=  leveldb
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)

#include $(CLEAR_VARS)
#LOCAL_MODULE := leveldb
#LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libleveldb.so
#LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
#include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := leveldb
LOCAL_CFLAGS := -D_REENTRANT -DOS_ANDROID -DLEVELDB_PLATFORM_POSIX -DNDEBUG -DSNAPPY
LOCAL_CPP_EXTENSION := .cc
LOCAL_C_INCLUDES := $(LOCAL_PATH)/leveldb-ndk $(LOCAL_PATH)/leveldb-ndk/include $(LOCAL_PATH)/snappy
LOCAL_SRC_FILES := leveldb-ndk/db/builder.cc leveldb-ndk/db/c.cc leveldb-ndk/db/db_impl.cc leveldb-ndk/db/db_iter.cc leveldb-ndk/db/dbformat.cc leveldb-ndk/db/filename.cc leveldb-ndk/db/log_reader.cc leveldb-ndk/db/log_writer.cc leveldb-ndk/db/memtable.cc leveldb-ndk/db/repair.cc leveldb-ndk/db/table_cache.cc leveldb-ndk/db/version_edit.cc leveldb-ndk/db/version_set.cc leveldb-ndk/db/write_batch.cc leveldb-ndk/table/block.cc leveldb-ndk/table/block_builder.cc leveldb-ndk/table/filter_block.cc leveldb-ndk/table/format.cc leveldb-ndk/table/iterator.cc leveldb-ndk/table/merger.cc leveldb-ndk/table/table.cc leveldb-ndk/table/table_builder.cc leveldb-ndk/table/two_level_iterator.cc leveldb-ndk/util/arena.cc leveldb-ndk/util/bloom.cc leveldb-ndk/util/cache.cc leveldb-ndk/util/coding.cc leveldb-ndk/util/comparator.cc leveldb-ndk/util/crc32c.cc leveldb-ndk/util/env.cc leveldb-ndk/util/env_posix.cc leveldb-ndk/util/filter_policy.cc leveldb-ndk/util/hash.cc leveldb-ndk/util/histogram.cc leveldb-ndk/util/logging.cc leveldb-ndk/util/options.cc leveldb-ndk/util/status.cc leveldb-ndk/port/port_posix.cc
LOCAL_STATIC_LIBRARIES += snappy

include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := snappy
LOCAL_CPP_EXTENSION := .cc
LOCAL_SRC_FILES := snappy/snappy.cc snappy/snappy-c.cc snappy/snappy-sinksource.cc

include $(BUILD_STATIC_LIBRARY)