{
  'conditions':[
    ['OS=="linux"', {
      'target_defaults': {
        'cflags': ['-fPIC', '-g', '-O3',],
        'include_dirs': ['../third_party/db-stable/build_unix', '../third_party/db-stable',],
        'defines':['_GNU_SOURCE', '_REENTRANT'],
        'libraries':['-lpthread'],
      },
    },],
    ['OS=="win"', {
      'target_defaults': {
        'include_dirs': ['../third_party/db-stable/build_windows', '../third_party/db-stable',],
        'defines':['WIN32','_UNICODE', 'UNICODE', 'DIAGNOSTIC','CONFIG_TEST'],
        'msvs_settings': {
            'VCLinkerTool': {'GenerateDebugInformation': 'true',},
            'VCCLCompilerTool': {'DebugInformationFormat': '3',},
        },
        'libraries': ['-lws2_32.lib',],
      },
    },],
  ],
  'targets': [
  {
    'target_name': 'db',
    'type': 'static_library',
    'msvs_guid': '3ED35681-7B4E-4060-BBBB-30620CA0AAAA',
    'conditions':[
        ['OS=="linux"', {
          'sources':[
'../third_party/db-stable/os/os_abs.c',
'../third_party/db-stable/os/os_clock.c',
'../third_party/db-stable/os/os_config.c',
'../third_party/db-stable/os/os_cpu.c',
'../third_party/db-stable/os/os_dir.c',
'../third_party/db-stable/os/os_errno.c',
'../third_party/db-stable/os/os_fid.c',
'../third_party/db-stable/os/os_flock.c',
'../third_party/db-stable/os/os_fsync.c',
'../third_party/db-stable/os/os_getenv.c',
'../third_party/db-stable/os/os_handle.c',
'../third_party/db-stable/os/os_map.c',
'../third_party/db-stable/os/os_mkdir.c',
'../third_party/db-stable/os/os_open.c',
'../third_party/db-stable/os/os_rename.c',
'../third_party/db-stable/os/os_rw.c',
'../third_party/db-stable/os/os_seek.c',
'../third_party/db-stable/os/os_stat.c',
'../third_party/db-stable/os/os_truncate.c',
'../third_party/db-stable/os/os_unlink.c',
'../third_party/db-stable/os/os_yield.c',
'../third_party/db-stable/repmgr/repmgr_posix.c',
'../third_party/db-stable/mutex/mut_pthread.c',
          ],
        }],
        ['OS=="win"', {
          'sources':[
'../third_party/db-stable/os_windows/os_abs.c',
'../third_party/db-stable/os_windows/os_clock.c',
'../third_party/db-stable/os_windows/os_config.c',
'../third_party/db-stable/os_windows/os_cpu.c',
'../third_party/db-stable/os_windows/os_dir.c',
'../third_party/db-stable/os_windows/os_errno.c',
'../third_party/db-stable/os_windows/os_fid.c',
'../third_party/db-stable/os_windows/os_flock.c',
'../third_party/db-stable/os_windows/os_fsync.c',
'../third_party/db-stable/os_windows/os_getenv.c',
'../third_party/db-stable/os_windows/os_handle.c',
'../third_party/db-stable/os_windows/os_map.c',
'../third_party/db-stable/os_windows/os_mkdir.c',
'../third_party/db-stable/os_windows/os_open.c',
'../third_party/db-stable/os_windows/os_rename.c',
'../third_party/db-stable/os_windows/os_rw.c',
'../third_party/db-stable/os_windows/os_seek.c',
'../third_party/db-stable/os_windows/os_stat.c',
'../third_party/db-stable/os_windows/os_truncate.c',
'../third_party/db-stable/os_windows/os_unlink.c',
'../third_party/db-stable/os_windows/os_yield.c',
'../third_party/db-stable/mutex/mut_win32.c',
'../third_party/db-stable/repmgr/repmgr_windows.c',
'../third_party/db-stable/clib/strsep.c',
         ],
        }],
      ],
    'sources':[
'../third_party/db-stable/btree/bt_compact.c',
'../third_party/db-stable/btree/bt_compare.c',
'../third_party/db-stable/btree/bt_compress.c',
'../third_party/db-stable/btree/bt_conv.c',
'../third_party/db-stable/btree/bt_curadj.c',
'../third_party/db-stable/btree/bt_cursor.c',
'../third_party/db-stable/btree/bt_delete.c',
'../third_party/db-stable/btree/bt_method.c',
'../third_party/db-stable/btree/bt_open.c',
'../third_party/db-stable/btree/bt_put.c',
'../third_party/db-stable/btree/bt_rec.c',
'../third_party/db-stable/btree/bt_reclaim.c',
'../third_party/db-stable/btree/bt_recno.c',
'../third_party/db-stable/btree/btree_auto.c',
'../third_party/db-stable/btree/bt_rsearch.c',
'../third_party/db-stable/btree/bt_search.c',
'../third_party/db-stable/btree/bt_split.c',
'../third_party/db-stable/btree/bt_stat.c',
'../third_party/db-stable/btree/bt_upgrade.c',
'../third_party/db-stable/btree/bt_verify.c',
'../third_party/db-stable/clib/snprintf.c',
'../third_party/db-stable/common/db_byteorder.c',
'../third_party/db-stable/common/db_compint.c',
'../third_party/db-stable/common/db_err.c',
'../third_party/db-stable/common/db_getlong.c',
'../third_party/db-stable/common/db_idspace.c',
'../third_party/db-stable/common/db_log2.c',
'../third_party/db-stable/common/db_shash.c',
'../third_party/db-stable/common/dbt.c',
'../third_party/db-stable/common/mkpath.c',
'../third_party/db-stable/common/openflags.c',
'../third_party/db-stable/common/os_method.c',
'../third_party/db-stable/common/zerofill.c',
'../third_party/db-stable/common/util_cache.c',
'../third_party/db-stable/common/util_sig.c',
'../third_party/db-stable/common/util_arg.c',
'../third_party/db-stable/common/util_log.c',
'../third_party/db-stable/crypto/aes_method.c',
'../third_party/db-stable/crypto/crypto.c',
'../third_party/db-stable/crypto/mersenne/mt19937db.c',
'../third_party/db-stable/crypto/rijndael/rijndael-alg-fst.c',
'../third_party/db-stable/crypto/rijndael/rijndael-api-fst.c',
'../third_party/db-stable/db/crdel_auto.c',
'../third_party/db-stable/db/crdel_rec.c',
'../third_party/db-stable/db/db_am.c',
'../third_party/db-stable/db/db_auto.c',
'../third_party/db-stable/db/db.c',
'../third_party/db-stable/db/db_cam.c',
'../third_party/db-stable/db/db_cds.c',
'../third_party/db-stable/db/db_conv.c',
'../third_party/db-stable/db/db_dispatch.c',
'../third_party/db-stable/db/db_dup.c',
'../third_party/db-stable/db/db_iface.c',
'../third_party/db-stable/db/db_join.c',
'../third_party/db-stable/db/db_meta.c',
'../third_party/db-stable/db/db_method.c',
'../third_party/db-stable/db/db_open.c',
'../third_party/db-stable/db/db_overflow.c',
'../third_party/db-stable/db/db_ovfl_vrfy.c',
'../third_party/db-stable/db/db_pr.c',
'../third_party/db-stable/db/db_rec.c',
'../third_party/db-stable/db/db_reclaim.c',
'../third_party/db-stable/db/db_remove.c',
'../third_party/db-stable/db/db_rename.c',
'../third_party/db-stable/db/db_ret.c',
'../third_party/db-stable/db/db_setid.c',
'../third_party/db-stable/db/db_setlsn.c',
'../third_party/db-stable/db/db_sort_multiple.c',
'../third_party/db-stable/db/db_stati.c',
'../third_party/db-stable/db/db_truncate.c',
'../third_party/db-stable/db/db_upg.c',
'../third_party/db-stable/db/db_upg_opd.c',
'../third_party/db-stable/db/db_vrfy.c',
'../third_party/db-stable/db/db_vrfyutil.c',
'../third_party/db-stable/dbm/dbm.c',
'../third_party/db-stable/db/partition.c',
'../third_party/db-stable/dbreg/dbreg_auto.c',
'../third_party/db-stable/dbreg/dbreg.c',
'../third_party/db-stable/dbreg/dbreg_rec.c',
'../third_party/db-stable/dbreg/dbreg_stat.c',
'../third_party/db-stable/dbreg/dbreg_util.c',
'../third_party/db-stable/env/env_alloc.c',
'../third_party/db-stable/env/env_config.c',
'../third_party/db-stable/env/env_failchk.c',
'../third_party/db-stable/env/env_file.c',
'../third_party/db-stable/env/env_globals.c',
'../third_party/db-stable/env/env_method.c',
'../third_party/db-stable/env/env_name.c',
'../third_party/db-stable/env/env_open.c',
'../third_party/db-stable/env/env_recover.c',
'../third_party/db-stable/env/env_region.c',
'../third_party/db-stable/env/env_register.c',
'../third_party/db-stable/env/env_sig.c',
'../third_party/db-stable/env/env_stat.c',
'../third_party/db-stable/fileops/fileops_auto.c',
'../third_party/db-stable/fileops/fop_basic.c',
'../third_party/db-stable/fileops/fop_rec.c',
'../third_party/db-stable/fileops/fop_util.c',
'../third_party/db-stable/hash/hash_auto.c',
'../third_party/db-stable/hash/hash.c',
'../third_party/db-stable/hash/hash_conv.c',
'../third_party/db-stable/hash/hash_dup.c',
'../third_party/db-stable/hash/hash_func.c',
'../third_party/db-stable/hash/hash_meta.c',
'../third_party/db-stable/hash/hash_method.c',
'../third_party/db-stable/hash/hash_open.c',
'../third_party/db-stable/hash/hash_page.c',
'../third_party/db-stable/hash/hash_rec.c',
'../third_party/db-stable/hash/hash_reclaim.c',
'../third_party/db-stable/hash/hash_stat.c',
'../third_party/db-stable/hash/hash_upgrade.c',
'../third_party/db-stable/hash/hash_verify.c',
'../third_party/db-stable/hmac/hmac.c',
'../third_party/db-stable/hmac/sha1.c',
'../third_party/db-stable/hsearch/hsearch.c',
'../third_party/db-stable/lock/lock.c',
'../third_party/db-stable/lock/lock_deadlock.c',
'../third_party/db-stable/lock/lock_failchk.c',
'../third_party/db-stable/lock/lock_id.c',
'../third_party/db-stable/lock/lock_list.c',
'../third_party/db-stable/lock/lock_method.c',
'../third_party/db-stable/lock/lock_region.c',
'../third_party/db-stable/lock/lock_stat.c',
'../third_party/db-stable/lock/lock_timer.c',
'../third_party/db-stable/lock/lock_util.c',
'../third_party/db-stable/log/log_archive.c',
'../third_party/db-stable/log/log.c',
'../third_party/db-stable/log/log_compare.c',
'../third_party/db-stable/log/log_debug.c',
'../third_party/db-stable/log/log_get.c',
'../third_party/db-stable/log/log_method.c',
'../third_party/db-stable/log/log_put.c',
'../third_party/db-stable/log/log_stat.c',
'../third_party/db-stable/mp/mp_alloc.c',
'../third_party/db-stable/mp/mp_bh.c',
'../third_party/db-stable/mp/mp_fget.c',
'../third_party/db-stable/mp/mp_fmethod.c',
'../third_party/db-stable/mp/mp_fopen.c',
'../third_party/db-stable/mp/mp_fput.c',
'../third_party/db-stable/mp/mp_fset.c',
'../third_party/db-stable/mp/mp_method.c',
'../third_party/db-stable/mp/mp_mvcc.c',
'../third_party/db-stable/mp/mp_region.c',
'../third_party/db-stable/mp/mp_register.c',
'../third_party/db-stable/mp/mp_resize.c',
'../third_party/db-stable/mp/mp_stat.c',
'../third_party/db-stable/mp/mp_sync.c',
'../third_party/db-stable/mp/mp_trickle.c',
'../third_party/db-stable/mutex/mut_alloc.c',
'../third_party/db-stable/mutex/mut_failchk.c',
'../third_party/db-stable/mutex/mut_method.c',
'../third_party/db-stable/mutex/mut_region.c',
'../third_party/db-stable/mutex/mut_stat.c',
'../third_party/db-stable/mutex/mut_tas.c',
'../third_party/db-stable/os/os_abort.c',
'../third_party/db-stable/os/os_addrinfo.c',
'../third_party/db-stable/os/os_alloc.c',
'../third_party/db-stable/os/os_ctime.c',
'../third_party/db-stable/os/os_pid.c',
'../third_party/db-stable/os/os_root.c',
'../third_party/db-stable/os/os_rpath.c',
'../third_party/db-stable/os/os_stack.c',
'../third_party/db-stable/os/os_tmpdir.c',
'../third_party/db-stable/os/os_uid.c',
'../third_party/db-stable/qam/qam_auto.c',
'../third_party/db-stable/qam/qam.c',
'../third_party/db-stable/qam/qam_conv.c',
'../third_party/db-stable/qam/qam_files.c',
'../third_party/db-stable/qam/qam_method.c',
'../third_party/db-stable/qam/qam_open.c',
'../third_party/db-stable/qam/qam_rec.c',
'../third_party/db-stable/qam/qam_stat.c',
'../third_party/db-stable/qam/qam_upgrade.c',
'../third_party/db-stable/qam/qam_verify.c',
'../third_party/db-stable/repmgr/repmgr_auto.c',
'../third_party/db-stable/repmgr/repmgr_elect.c',
'../third_party/db-stable/repmgr/repmgr_method.c',
'../third_party/db-stable/repmgr/repmgr_msg.c',
'../third_party/db-stable/repmgr/repmgr_net.c',
'../third_party/db-stable/repmgr/repmgr_queue.c',
'../third_party/db-stable/repmgr/repmgr_sel.c',
'../third_party/db-stable/repmgr/repmgr_stat.c',
'../third_party/db-stable/repmgr/repmgr_util.c',
'../third_party/db-stable/rep/rep_auto.c',
'../third_party/db-stable/rep/rep_backup.c',
'../third_party/db-stable/rep/rep_elect.c',
'../third_party/db-stable/rep/rep_lease.c',
'../third_party/db-stable/rep/rep_log.c',
'../third_party/db-stable/rep/rep_method.c',
'../third_party/db-stable/rep/rep_record.c',
'../third_party/db-stable/rep/rep_region.c',
'../third_party/db-stable/rep/rep_stat.c',
'../third_party/db-stable/rep/rep_util.c',
'../third_party/db-stable/rep/rep_verify.c',
'../third_party/db-stable/sequence/seq_stat.c',
'../third_party/db-stable/sequence/sequence.c',
'../third_party/db-stable/txn/txn_auto.c',
'../third_party/db-stable/txn/txn.c',
'../third_party/db-stable/txn/txn_chkpt.c',
'../third_party/db-stable/txn/txn_failchk.c',
'../third_party/db-stable/txn/txn_method.c',
'../third_party/db-stable/txn/txn_rec.c',
'../third_party/db-stable/txn/txn_recover.c',
'../third_party/db-stable/txn/txn_region.c',
'../third_party/db-stable/txn/txn_stat.c',
'../third_party/db-stable/txn/txn_util.c',
],
  },
  {
    'target_name': 'db_stat',
    'type': 'executable',
    'msvs_guid': '3ED35681-7B4E-4060-BBBB-30620CA0BBBB',
    'dependencies': ['db',],
    'sources':[
'../third_party/db-stable/clib/getopt.c',
'../third_party/db-stable/db_stat/db_stat.c',
    ],
  },
  ],
}