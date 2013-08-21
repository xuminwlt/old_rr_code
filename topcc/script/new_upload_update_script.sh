#!/bin/sh

usage() {
echo "$0 --switch-port"
echo "  switch the server from one port to the other"
echo "$0 --update-conf"
echo "  copy new conf from pool and update service"
echo "$0 --update-bin=new-bin-name"
echo "  copy new binary excutable and conf from pool and update service"
exit 0
}

upload_new_pool="10.7.18.21:/data/release/bin"
upload_bin_dir="/opt/upload_service/bin"
nginx_conf_dir="/etc/nginx"
working_port=""
working_fcgi_bin=""
idle_fcgi_bin=""
idle_port=""
new_fcgi_bin=""
new_fcgi_bin_cp=""
err_detect_port=1
err_detect_fcgi_bin=2
err_scp=3
err_check_dep=4


backup_conf() {
  cd $upload_bin_dir/conf
  cp service.conf service.conf.bak
  cp template.conf template.conf.bak
  cp diskcache.conf diskcache.conf.bak
}
recover_conf() {
  cd $upload_bin_dir/conf
  cp service.conf.bak service.conf
  cp template.conf.bak template.conf
  cp diskcache.conf.bak diskcache.conf
}
scp_conf() {
  scp -o "StrictHostKeyChecking no" $upload_new_pool/conf/service.conf $upload_bin_dir/conf
  scp -o "StrictHostKeyChecking no" $upload_new_pool/conf/template.conf $upload_bin_dir/conf
  scp -o "StrictHostKeyChecking no" $upload_new_pool/conf/diskcache.conf $upload_bin_dir/conf
}
scp_bin() {
  if ! scp -o "StrictHostKeyChecking no" $upload_new_pool/$new_fcgi_bin $upload_bin_dir/; then
    echo "copy new binary failed"
    exit $err_scp
  fi
  cd $upload_bin_dir
  cp $new_fcgi_bin $new_fcgi_bin_cp
}
check_lib() {
  cd $upload_bin_dir
  if ldd $new_fcgi_bin | grep found; then
    echo "dependency check failed"
    exit $err_check_dep
  fi
}
detect_port() {
  echo "detect port ..."
  if cat /etc/nginx/nginx.conf | grep -E "^[[:space:]]*server.*8085"; then
    working_port="8085"
    idle_port="8086"
  elif cat /etc/nginx/nginx.conf | grep -E "^[[:space:]]*server.*8086"; then
    working_port="8086"
    idle_port="8085"
  else
    echo "can not detect working port"
    exit $err_detect_port
  fi
  echo "working_port: "$working_port", idle_port: "$idle_port
}
detect_fcgi_bin() {
  idle_fcgi_bin=`ps aux | grep $idle_port | grep "\-p" | grep "\-F" | grep -v grep | awk '{print $11}' | sort -u`
  # trim "./" if it starts with it
  idle_fcgi_bin=${idle_fcgi_bin#*"./"}
  working_fcgi_bin=`ps aux | grep $working_port | grep "\-p" | grep "\-F" | grep -v grep | awk '{print $11}' | sort -u`
  working_fcgi_bin=${working_fcgi_bin#*"./"}
  if [ ${#working_fcgi_bin} -eq 0 ]; then
    echo "detect working fcgi bin failed"
    exit $err_detect_fcgi_bin
  fi 
  if [ ${#idle_fcgi_bin} -eq 0 ]; then
    echo "detect idle fcgi bin failed"
    exit $err_detect_fcgi_bin
  fi 
  echo "working_bin: "$working_fcgi_bin", idle_bin: "$idle_fcgi_bin
}
check_precondition() {
  detect_port
  detect_fcgi_bin
}
kill_fcgi() {
  echo "kill $1 ..."
  while ps aux | grep "\b"$1"\b" | grep "\-p" | grep "\-F" | grep -v grep; do 
    killall -9 $1 >/dev/null
    sleep 1 
  done
  sleep 1 
}
start_fcgi() {
  cd $upload_bin_dir
  cmd="./$1 -l /data/log/$1.log -p $2 -F 24"
  echo $cmd
  $cmd
}
update_nginx_conf() {
  cd $nginx_conf_dir
  cp $1 nginx.conf
}
restart_nginx() {
  nginx -s reload
}

#
switch_port() {
  check_precondition
  kill_fcgi $idle_fcgi_bin
  start_fcgi $idle_fcgi_bin $idle_port
  update_nginx_conf $idle_port.conf
  restart_nginx
}
update_conf() {
  check_precondition
  backup_conf
  scp_conf
  kill_fcgi $idle_fcgi_bin
  start_fcgi $idle_fcgi_bin $idle_port
  update_nginx_conf $idle_port.conf
  restart_nginx
}
update_bin() {
  scp_bin
  check_lib
  check_precondition
  backup_conf
  scp_conf
  kill_fcgi $idle_fcgi_bin
  start_fcgi $new_fcgi_bin $idle_port
  update_nginx_conf $idle_port.conf
  restart_nginx
  sleep 10
  kill_fcgi $working_fcgi_bin
  start_fcgi $new_fcgi_bin_cp $working_port
}

# main entry
if [ $# -eq 0 ]; then
	usage
fi

if ! options=$(getopt -o h -l help,switch-port,update-conf,update-bin: -- "$@")
then 
	exit 1
fi
set -- $options

while [ $# -gt 0 ]
do
	case $1 in
		-h|--help) usage;;
		--switch-port) switch_port;;
		--update-conf) update_conf;;
		--update-bin) new_fcgi_bin=`echo $2 | tr -d "'"`;new_fcgi_bin_cp=$new_fcgi_bin"cp";update_bin; shift;;
		(--) shift; break;;
		(-*) echo "$0: error - unrecognized option $1" 1>&2; exit 1;;
		(*) break;;
	esac
	shift
done