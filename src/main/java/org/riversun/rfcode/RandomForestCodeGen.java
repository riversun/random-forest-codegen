package org.riversun.rfcode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyUnicode;
import org.python.util.PythonInterpreter;

/**
 * Tool to generate source code from tree model information of randomforest
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 *
 */
public class RandomForestCodeGen {

    public static void main(String[] args) throws IOException {

        if (true) {
            RandomForestCodeGen.Builder builder = new RandomForestCodeGen.Builder();

            // create the Options
            Options options = new Options();

            options.addOption(Option.builder("p")
                    .argName("packageName")
                    .hasArg(true)
                    .required(false)
                    .desc("package name of generated source code")
                    .longOpt("package")
                    .build());

            options.addOption(Option.builder("c")
                    .argName("className")
                    .hasArg(true)
                    .required(false)
                    .desc("class name of generated source code")
                    .longOpt("class")
                    .build());

            options.addOption(Option.builder("m")
                    .argName("mainMethod")
                    .hasArg(false)
                    .desc("generate main method")
                    .longOpt("main")
                    .build());

            options.addOption(Option.builder("l")
                    .argName("language")
                    .hasArg(true)
                    .desc("language('java' only)")
                    .longOpt("language")
                    .build());
            options.addOption(Option.builder("f")
                    .argName("treeFile")
                    .hasArg(true)
                    .desc("tree model file path")
                    .longOpt("inFile")
                    .build());
            options.addOption(Option.builder("o")
                    .argName("outDir")
                    .hasArg(true)
                    .desc("output directory for generated source code")
                    .longOpt("outDir")
                    .build());

            if (args != null && args.length == 0) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("options", options);
            }

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = null;
            try {
                cmd = parser.parse(options, args);

                if (cmd.hasOption("p")) {
                    builder.packageName(cmd.getOptionValue("p"));
                }
                if (cmd.hasOption("c")) {
                    builder.className(cmd.getOptionValue("c"));
                }
                if (cmd.hasOption("m")) {
                    builder.mainMethod(true);
                }
                if (cmd.hasOption("f")) {

                    builder.treeFile(cmd.getOptionValue("f"));

                } else {
                    System.err.println("Pleaes specify tree model file with '-f [tree_file]'");
                    System.exit(0);
                }

                System.out.println("Generating source code ...");

                if (cmd.hasOption("o")) {
                    RandomForestCodeGen o = builder.build();
                    File f = new File(cmd.getOptionValue("o"));
                    File generateFile = o.generateFile(f);
                    System.out.println("Finished generating outFile=" + generateFile.getAbsolutePath());
                } else {
                    System.err.println("Pleaes specify output directory with '-o [output_dir]'");
                    System.exit(0);
                }
            } catch (ParseException e) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("options", options);
            }
        }

    }

    private static final String UTF_8 = StandardCharsets.UTF_8.name();

    private String classifierName;
    private String packageName;
    private String rfTreeModel;
    private boolean generateMainMethod;
    private String targetLanguage;

    static class Builder {

        private String packageName = "com.example";
        private String classifierName = "RandomForestEngine";
        private String rfTreeModel;
        private boolean generateMainMethod = false;
        private String targetLanguage = "java";

        public Builder() {
        }

        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return Builder.this;
        }

        public Builder className(String className) {
            this.classifierName = className;
            return Builder.this;
        }

        public Builder tree(String rfTreeModel) {
            this.rfTreeModel = rfTreeModel;
            return Builder.this;
        }

        public Builder treeFile(String path) {
            String rfTreeModel;
            try {
                rfTreeModel = FileUtil.getTextFromStream(new FileInputStream(new File(path)));
                this.rfTreeModel = rfTreeModel;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return Builder.this;
        }

        public Builder mainMethod(boolean generateMainMethod) {
            this.generateMainMethod = generateMainMethod;
            return Builder.this;
        }

        public Builder targetLanguage(String targetLanguage) {
            this.targetLanguage = targetLanguage;
            return Builder.this;
        }

        RandomForestCodeGen build() {
            if (this.rfTreeModel == null) {
                throw new NullPointerException("TreeModel is not set.");
            }
            return new RandomForestCodeGen(this);
        }
    }

    public RandomForestCodeGen(Builder builder) {
        this.packageName = builder.packageName;
        this.classifierName = builder.classifierName;
        this.rfTreeModel = builder.rfTreeModel;
        this.generateMainMethod = builder.generateMainMethod;
    }

    public File generateFile(File dir) {

        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (dir.isDirectory()) {
            final File file = new File(dir, File.separator + this.classifierName + ".java");
            FileUtil.writeText(file, generate(), UTF_8, false);
            return file;
        } else {
            throw new RuntimeException("Please specify directory. dir=" + dir + " is not a directory");
        }

    }

    /**
     * Generate source code
     * 
     * @return
     */
    public String generate() {

        try {

            // Generate main method of source code
            String sampleHints = generateMainMethod ? generateMainMethod(rfTreeModel) : "";

            String converterPyScript = FileUtil.getTextFromResourceFile("to_java_source.txt");
            String libTemplateJavaCode = FileUtil.getTextFromResourceFile("java_template.txt");

            Properties props = new Properties();
            props.put("python.console.encoding", UTF_8);

            PythonInterpreter.initialize(System.getProperties(), props, new String[0]);

            try (PythonInterpreter pythonInterpreter = new PythonInterpreter()) {

                PyString pyClsName = new PyString(classifierName);
                pythonInterpreter.set("classifier_class_name", pyClsName);

                PyString pyPackageName = new PyString(packageName);
                pythonInterpreter.set("PACKAGE_NAME", pyPackageName);

                PyString pyLibSourceCode = new PyString(libTemplateJavaCode);
                pythonInterpreter.set("lib_source_code", pyLibSourceCode);

                // Use "PyUnicode" for 2-byte char(ex. Kanji)
                PyUnicode pyWekaRandomForestTreeDefinition = new PyUnicode(rfTreeModel);
                pythonInterpreter.set("random_forest_weka_tree", pyWekaRandomForestTreeDefinition);

                PyUnicode sampleCodeHint = new PyUnicode(sampleHints);
                pythonInterpreter.set("sample_hint", sampleCodeHint);

                pythonInterpreter.exec(converterPyScript);

                // Get result from python
                PyObject pyObjResult = pythonInterpreter.get("result_string");
                PyString spyStrResult = (PyString) pyObjResult;
                String result = spyStrResult.asString();

                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private String generateMainMethod(String rfTreeModel) {

        final StringBuilder sb = new StringBuilder();

        try {
            InputStream bais = new ByteArrayInputStream(rfTreeModel.getBytes(UTF_8));
            List<String> lines;

            sb.append("public static void main(String[] args) throws Exception {\n");
            sb.append("\n");
            sb.append(String.format(" %s rf = new %s();\n", classifierName, classifierName));
            sb.append("\n");

            lines = FileUtil.readTextAsList(bais, UTF_8);

            int lineNum4AttrLabels = -1;
            int lineNum4AttrValues = -1;
            int lineNum4ObjectiveAttr = -1;

            for (int lineNum = 0; lineNum < lines.size(); lineNum++) {
                String line = lines.get(lineNum);
                if (line.equals("=== Begin of sample ===")) {
                    lineNum4AttrLabels = lineNum + 1;
                    lineNum4AttrValues = lineNum4AttrLabels + 1;
                    lineNum4ObjectiveAttr = lineNum4AttrValues + 1;
                    break;
                }
            }

            if (lineNum4AttrLabels >= 0) {

                String[] labels = lines.get(lineNum4AttrLabels).split(",");
                String[] samples = lines.get(lineNum4AttrValues).split(",");
                int objectiveAttrIdx = Integer.parseInt(lines.get(lineNum4ObjectiveAttr));
                String objectiveAttrLabel = labels[objectiveAttrIdx];
                sb.append(" // Set attribute values for prediction\n");

                for (int attrIdx = 0; attrIdx < labels.length; attrIdx++) {
                    if (attrIdx != objectiveAttrIdx) {
                        sb.append(String.format(" rf.%s=%s;\n", labels[attrIdx], samples[attrIdx]));
                    }
                }
                sb.append("\n");
                sb.append(String.format(" //Perform '%s' prediction\n", objectiveAttrLabel));
            }

            sb.append(" Prediction prediction = rf.runClassification();\n");
            sb.append("\n");
            sb.append(" System.out.println(\"prediction=\"+prediction);\n");
            sb.append("\n");
            sb.append("}\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
