#ifndef ARCH_BASE_CIRCULAR_COUNT_H__
#define ARCH_BASE_CIRCULAR_COUNT_H__

namespace arch {

// 从0到count的轮询
struct circular_count {
  circular_count(size_t count) : i_(0), count_(count) {}

  size_t operator()() {
    size_t r = i_++;
    if (i_ >= count_)
      i_ = 0;
    return r;
  }

private:
  size_t i_;
  const size_t count_;
};

}; // namespace arch

#endif // ARCH_BASE_CIRCULAR_COUNT_H__
