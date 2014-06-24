package com.DeathByCaptcha;


public class AccessDeniedException extends Exception
{
    private static final long serialVersionUID = -1777297167562301082L;

    public AccessDeniedException(String message)
    {
        super(message);
    }
}
