#ifndef WERE_SIGNAL_HANDLER_H
#define WERE_SIGNAL_HANDLER_H

#include "were.h"

class were_fd;

class were_signal_handler : virtual public were_object
{
public:
    ~were_signal_handler() override;
    were_signal_handler();

signals:
    were_signal<void (uint32_t number)> signal;

private:
    were_pointer<were_fd> fd_;
};

#endif // WERE_SIGNAL_HANDLER_H
