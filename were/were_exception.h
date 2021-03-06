#ifndef WERE_EXCEPTION_H
#define WERE_EXCEPTION_H

#include <exception>
#include <cerrno>
#include <cstring>


class were_exception : public std::exception
{
public:
    ~were_exception() override;
    were_exception();
    explicit were_exception(const char *format, ...);

    const char *what() const noexcept override
    {
        return what_;
    }

private:
    char *what_;
};

#define WE_SIMPLE "%s:%d", __FILE__, __LINE__
#define WE_SIMPLE_ERRNO "%s:%d %d (%s)", __FILE__, __LINE__, errno, strerror(errno)


#endif // WERE_EXCEPTION_H
