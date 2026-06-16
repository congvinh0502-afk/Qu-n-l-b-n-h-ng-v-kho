@echo off
:: ===================================================================
:: Khởi động QLBH - Quản lý Bán hàng & Kho (Nhóm 6)
:: Yêu cầu: Java 17+ đã cài, Maven đã chạy ít nhất 1 lần (để có cache)
:: ===================================================================
setlocal enabledelayedexpansion

set JAR=C:\qlbh-build\qlbh.jar
set M2=%USERPROFILE%\.m2\repository\org\openjfx
set VER=21

:: Kiểm tra file jar
if not exist "%JAR%" (
    echo [ERROR] Chua tim thay %JAR%
    echo Hay chay lenh:  mvn package -Djavax.net.ssl.trustStoreType=WINDOWS-ROOT
    pause
    exit /b 1
)

set FX_PATH=%M2%\javafx-base\%VER%\javafx-base-%VER%-win.jar;%M2%\javafx-controls\%VER%\javafx-controls-%VER%-win.jar;%M2%\javafx-fxml\%VER%\javafx-fxml-%VER%-win.jar;%M2%\javafx-graphics\%VER%\javafx-graphics-%VER%-win.jar

:: Kiểm tra JavaFX có trong cache Maven chưa
if not exist "%M2%\javafx-base\%VER%\javafx-base-%VER%-win.jar" (
    echo [ERROR] Chua tim thay JavaFX %VER% trong Maven cache.
    echo Hay chay lenh:  mvn javafx:run -Djavax.net.ssl.trustStoreType=WINDOWS-ROOT  mot lan de tai ve.
    pause
    exit /b 1
)

echo Dang khoi dong QLBH...
java -Djavax.net.ssl.trustStoreType=WINDOWS-ROOT ^
     --module-path "%FX_PATH%" ^
     --add-modules javafx.controls,javafx.fxml ^
     -jar "%JAR%"
