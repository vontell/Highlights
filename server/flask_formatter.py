from flask import Response

class Formatter(Response):
    formatters = {}
    
    @classmethod
    def force_type(cls, rv, environ=None):
        t = type(rv) 
        if t in cls.formatters:
            rv = cls.formatters[t](rv)
        return super(Formatter, cls).force_type(rv, environ)
        
    @classmethod
    def of(cls, *types):
        def register_formatter(f):
            for t in types:
                cls.formatters[t] = f
            return f
        return register_formatter