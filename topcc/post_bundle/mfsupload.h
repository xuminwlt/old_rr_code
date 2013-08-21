#ifndef _MFS_UPLOAD_H
#define _MFS_UPLOAD_H


#include <string>
#include <vector>
#include <list>

#include<boost/thread/thread.hpp>
#include<boost/thread/condition.hpp>
#include<boost/shared_ptr.hpp>
#include<boost/date_time/posix_time/posix_time.hpp>
#include<boost/asio.hpp>
#include<boost/bind.hpp>
#include "mfsbundle/bundle/bundle.h"

namespace net {

typedef std::pair<std::string ,size_t> FileData;
typedef std::pair<std::string,int>Address;

class MfsPost {
  public:
    MfsPost();
    bool Start();
    bool PostFile(const FileData& filedata);
    void Close();
    std::list<FileData>jobs_;
    int now_jobs_;
  
  private:
    void Clear(){};
    void HandleNewJob(const FileData &filedata);
    bool will_exit_;
       int max_jobs_;
    boost::shared_ptr<bundle::Writer> writer_;
    boost::shared_ptr<boost::thread> work_;
    boost::asio::io_service ios_;
};

struct MfsPool{
  bool Start();
  bool PostFile(const FileData &filedata);
  void Close();
  std::vector<boost::shared_ptr<MfsPost> > mp_;
  int max_works_;
  int current_;
};

}

#endif
