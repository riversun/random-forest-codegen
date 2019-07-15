package org.riversun.rfcode;

import java.io.File;
import java.io.FileInputStream;

/**
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 *
 */
class _CodeGenExample {

    public static void main(String[] args) throws Exception {

        String rfTreeModel = FileUtil.getTextFromStream(new FileInputStream(new File("c:/temp/model.txt")));

        RandomForestCodeGen o = new RandomForestCodeGen.Builder()
                .packageName("org.example")// package name
                .className("RandomForestExample")// class name
                .mainMethod(true)// true:generate main method
                .targetLanguage("java")// target language(java only)
                .tree(rfTreeModel)// String data of tree model
                // .treeFile("model.txt")//tree model file
                .build();

        String generateSourceCode = o.generate();
        o.generateFile(new File("c:/temp"));

        System.out.println(generateSourceCode);
    }
}
