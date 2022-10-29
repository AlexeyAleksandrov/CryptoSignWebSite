@rem
@rem Copyright 2004-2008 Crypto-Pro. All rights reserved.
@rem ���� ���� �������� ����������, ����������
@rem �������������� �������� ������-���.
@rem
@rem ����� ����� ����� ����� �� ����� ���� �����������,
@rem ����������, ���������� �� ������ �����,
@rem ������������ ��� �������������� ����� ��������,
@rem ���������������, �������� �� ���� � ��� ��
@rem ����� ������������ ������� ��� ����������������
@rem ���������� ���������� � ��������� ������-���.
@rem
@rem ---------------------------------------------------
@rem
@rem ������ ������� ����������� ������ CryptoPro JCP v.1.0
@rem 
@rem �������������:
@rem   ControlPane.bat <����_�_JRE>
@rem
@rem ������:
@rem   ControlPane.bat "E:\Program Files\Java\jdk1.5.0\jre"
@rem

@if not "-%~1"=="-" @goto :setjavacmd
@echo USAGE:
@echo   ControlPane.bat path_to_JRE
@goto :EOF

:setjavacmd
@set JAVACMD=java
@set JREDIR=
@if not "-%~1"=="-" @set JREDIR=%~1
@if not "-%JREDIR%"=="-" @goto :checkjre
@goto :controlpane

:checkjre
@set JAVACMD="%JREDIR%\bin\java.exe"
@if exist %JAVACMD% @goto :controlpane
@echo File not found: %JAVACMD%
@goto :ERROR

:controlpane
@echo ---- Starting control pane
@%JAVACMD% -cp .;*; ru.CryptoPro.JCP.ControlPane.MainControlPane
@if "%errorlevel%"=="0" @goto :cpend
@echo ---- Starting control pane failed
@goto :ERROR

:cpend
@echo ---- Control pane finished
@echo ---- Script SUCCEEDED
@goto :EOF

:ERROR
@echo ---- Script ERROR
@goto :EOF
