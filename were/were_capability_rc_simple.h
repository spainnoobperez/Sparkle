#ifndef WERE_CAPABILITY_RC_SIMPLE_H
#define WERE_CAPABILITY_RC_SIMPLE_H

#include "were_capability_rc.h"
#include <atomic>

class were_capability_rc_simple : virtual public were_capability_rc
{
public:
    virtual ~were_capability_rc_simple() {}
    were_capability_rc_simple();

    int reference_count() const override;

private:
    void reference() override;
    void unreference() override;

private:
    std::atomic<int> reference_count_;
};

#endif // WERE_CAPABILITY_RC_SIMPLE_H
