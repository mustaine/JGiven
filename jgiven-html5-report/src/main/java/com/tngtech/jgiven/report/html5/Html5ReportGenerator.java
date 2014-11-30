package com.tngtech.jgiven.report.html5;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.tngtech.jgiven.impl.util.ResourceUtil;
import com.tngtech.jgiven.report.impl.FileGenerator;
import com.tngtech.jgiven.report.json.JsonModelTraverser;
import com.tngtech.jgiven.report.json.ReportModelFileHandler;
import com.tngtech.jgiven.report.model.ReportModel;

public class Html5ReportGenerator implements ReportModelFileHandler, FileGenerator {
    private static final Logger log = LoggerFactory.getLogger( Html5ReportGenerator.class );

    private Appendable writer;

    @Override
    public void handleReportModel( ReportModel model, File file ) {
        new Gson().toJson( model, writer );
        try {
            writer.append( "," );
        } catch( IOException e ) {
            throw Throwables.propagate( e );
        }
    }

    public void generate( File toDir, File sourceDir ) throws IOException {
        log.info( "Generating HTML5 report to {}...", toDir );

        unzipApp( toDir );
        generateAllScenarios( toDir, sourceDir );
        generateMetaData( toDir );
    }

    static class MetaData {
        Date created = new Date();
    }

    private void generateMetaData( File toDir ) throws IOException {
        File metaDataFile = new File( toDir, "metaData.js" );
        log.debug( "Generating " + metaDataFile + "..." );
        MetaData metaData = new MetaData();

        String content = "jgivenReport.setMetaData(" + new Gson().toJson( metaData ) + " );";

        Files.write( content, metaDataFile, Charsets.UTF_8 );

    }

    private void generateAllScenarios( File toDir, File sourceDir ) throws IOException {
        File targetFile = new File( toDir, "allScenarios.js" );
        log.debug( "Generating " + targetFile + "..." );

        PrintStream printStream = new PrintStream( new FileOutputStream( targetFile ), false, "utf-8" );
        this.writer = printStream;

        try {
            this.writer.append( "jgivenReport.setAllScenarios([" );
            new JsonModelTraverser().traverseModels( sourceDir, this );
            this.writer.append( "]);" );
            printStream.flush();
        } finally {
            ResourceUtil.close( printStream );
        }
    }

    private void unzipApp( File toDir ) throws IOException {
        String appZipPath = "/" + Html5ReportGenerator.class.getPackage().getName().replace( '.', '/' ) + "/app.zip";

        log.debug( "Unzipping {}...", appZipPath );

        InputStream inputStream = this.getClass().getResourceAsStream( appZipPath );
        ZipInputStream zipInputStream = new ZipInputStream( inputStream );

        ZipEntry entry;
        while( ( entry = zipInputStream.getNextEntry() ) != null ) {
            File file = new File( toDir, entry.getName() );

            if( entry.isDirectory() ) {
                if( !file.exists() ) {
                    log.debug( "Creating directory {}...", file );
                    if( !file.mkdirs() ) {
                        throw new IOException( "Could not create directory " + file );
                    }
                }
                continue;
            }
            log.debug( "Unzipping {}...", file );

            FileOutputStream fileOutputStream = new FileOutputStream( file );

            byte[] buffer = new byte[1024];

            int len;
            while( ( len = zipInputStream.read( buffer ) ) > 0 ) {
                fileOutputStream.write( buffer, 0, len );
            }

            fileOutputStream.close();
        }
    }
}