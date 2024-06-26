package com.star.maker.generator.main;


public class ZipGenerator extends GenerateTemplate{


    @Override
    protected String buildDist(String outputPath, String sourceCopyDestPath, String jarPath, String shellOutputFilePath) {


        String disPath = super.buildDist(outputPath, sourceCopyDestPath, jarPath, shellOutputFilePath);
        return super.buildZip(disPath);
    }


}
