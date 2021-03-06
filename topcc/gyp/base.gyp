{
  'variables': {
    'library': 'static_library',
  },
  'conditions': [
    ['OS=="linux"', {
      'target_defaults': {
        'cflags': ['-fPIC', '-g', '-O2',],
        'defines': ['OS_LINUX'],
      },
    },],
    ['OS=="win"', {
      'target_defaults': {
        # 'cflags': ['-fPIC', '-g', '-O2',],
        'defines': ['OS_WIN', 'NOMINMAX', 'UNICODE', '_UNICODE', 'WIN32_LEAN_AND_MEAN', '_WIN32_WINNT=0x0501'],
      },
    },],
  ],
  'targets': [
    {
      'target_name': 'base_naketest',
      'type': 'executable',
      'dependencies': [
        'base',
        '../testing/gtest.gyp:gtestmain',
      ],
      'conditions':[
        ['OS=="linux"', {'libraries': ['-lboost_system', '-lboost_thread', '-lpthread', '-ltcmalloc_minimal'] }],
      ],
      'include_dirs': ['..', '../testing/gtest/include',],
      'sources': [
        '../base/logging_test.cc',
      ],
    },
    {
      'target_name': 'base_unittest',
      'type': 'executable',
      'dependencies': [
        'base',
        '../testing/gtest.gyp:gtestmain',
      ],
      'conditions':[
        ['OS=="linux"', {'libraries': ['-lboost_system', '-lboost_thread', '-lpthread', '-ltcmalloc_minimal'] }],
      ],
      'sources': [
'../base/asyncall2_test.cc',
'../base/asyncall_test.cc',
'../base/atomicops_test.cc',
'../base/baseasync_test.cc',
'../base/boost_test.cc',
'../base/cache_test.cc',
'../base/common_test.cc',
'../base/directstream_test.cc',
'../base/doobs_hash_test.cc',
'../base/gtest_prod_util.h',
'../base/hash_test.cc',
'../base/jdbc_test.cc',
'../base/logging_test.cc',
'../base/logrotate_test.cc',
'../base/md5_test.cc',
'../base/messagequeue_test.cc',
'../base/mkdirs_test.cc',
'../base/pathops_test.cc',
'../base/pcount_test.cc',
'../base/ptime_test.cc',
'../base/rwlock_test.cc',
'../base/stringencode_test.cc',
'../base/tcmalloc_test.cc',
'../base/threadpool_test.cc',
'../base/types_test.cc',
      ],
      'include_dirs': [
        '..',
        '../testing/gtest/include',
        '/home/lqh/include'
      ],
    },
    {
      'target_name': 'base',
      'type': 'static_library',
      'msvs_guid': '9301A569-5D2B-4D11-9332-B1E30AEACB8D',
      'include_dirs': ['..','/home/lqh/include'],
      'dependencies': [
      ],
      'sources': [
'../base/asyncall.cc',
'../base/asyncall.h',
'../base/atomicops.h',
'../base/atomicops-internals-linuxppc.h',
'../base/atomicops-internals-macosx.h',
'../base/atomicops-internals-x86.cc',
'../base/atomicops-internals-x86.h',
'../base/atomicops-internals-x86-msvc.h',
'../base/baseasync.cc',
'../base/baseasync.h',
'../base/basictypes.h',
'../base/cache.h',
'../base/circular_count.h',
'../base/common.cc',
'../base/common.h',
'../base/compiler_specific.h',
'../base/directstream.h',
'../base/doobs_hash.cc',
'../base/doobs_hash.h',
'../base/eintr_wrapper.h',
'../base/escape.h',
'../base/getopt.c',
'../base/getopt.h',
'../base/globalinit.h',
'../base/hash.h',
'../base/hashmap.h',
'../base/jdbcurl.cc',
'../base/jdbcurl.h',
'../base/logging.cc',
'../base/logging.h',
'../base/logrotate.h',
'../base/md5.cc',
'../base/md5.h',
'../base/messagequeue.h',
'../base/mkdirs.cc',
'../base/mkdirs.h',
'../base/network.cc',
'../base/network.h',
'../base/no_exception.cc',
'../base/once.cc',
'../base/once.h',
'../base/pathops.cc',
'../base/pathops.h',
'../base/pcount.cc',
'../base/pcount.h',
'../base/port.h',
'../base/ptime.h',
'../base/README.txt',
'../base/rwlock.h',
'../base/scoped_ptr.h',
'../base/signals.cc',
'../base/signals.h',
'../base/startuplist.cc',
'../base/startuplist.h',
'../base/stringencode.cc',
'../base/stringencode.h',
'../base/string_piece.cc',
'../base/string_piece.h',
'../base/string_split.cc',
'../base/string_split.h',
'../base/string_tokenizer.h',
'../base/string_util.cc',
'../base/string_util.h',
'../base/third_party/dmg_fp/g_fmt.cc',
'../base/third_party/dmg_fp/dmg_fp.h',
'../base/third_party/dmg_fp/dtoa.cc',
'../base/third_party/dynamic_annotations/dynamic_annotations.c',
'../base/third_party/dynamic_annotations/dynamic_annotations.h',
'../base/thread.cc',
'../base/thread.h',
'../base/threadpool.cc',
'../base/threadpool.h',
      ],
    },
  ],
  'conditions': [
    ['OS=="linux"', {
      'target_defaults': {
      'sources!': ['',],
      'cflags': ['-fPIC', '-g', '-O2',],
      'defines': ['OS_LINUX'],
      },
    }],
    ['OS=="win"', {
      'target_defaults': {
      'sources!': ['',],
      'defines': ['OS_WIN', 'NOMINMAX', 'UNICODE', '_UNICODE', 'WIN32_LEAN_AND_MEAN', '_WIN32_WINNT=0x0501'],
      },
    }],
  ],
}
