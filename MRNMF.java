public class MRNMF {

    private static CLIOption[] opts = new CLIOption[] {
            new CLIOption("i", true, "path to the input matrix file; if not set random matrix with given sparsity -s will be generated", false),
            new CLIOption("s", true, "sparsity of random generated matrix", false),
            new CLIOption("o", true, "path to the output directory"),
            new CLIOption("t", true, "path to the temporary directory"),
            new CLIOption("n", true, "matrix row number"),
            new CLIOption("m", true, "matrix column number"),
            new CLIOption("it", true, "iterations number"),
            new CLIOption("k", true, "the dimension number to be reduced to"),
            new CLIOption("r", true, "the range value for the matrix elements"),
            new CLIOption("a", true, "the algorithm to be used"),
            new CLIOption("h", false, "print help for this application", false)
    };


    public static void usage() {
        System.out.print("Usage: hadoop jar MRNMF.jar nmf.com.lsdp.MRNMF\n");
        for (CLIOption o : opts) {
            System.out.println(o);
        }
    }

    public static void main(String[] args) throws ParseException, IOException, InterruptedException, ClassNotFoundException {

        Options options = new Options();
        for (CLIOption o : opts) {
            options.addOption(o.getName(), o.hasArg(), o.getDescription());
        }
        BasicParser parser = new BasicParser();
        CommandLine commandLine = parser.parse(options, args);

        if (commandLine.hasOption('h')) {
            usage();
            return;
        }

        for (CLIOption o : opts) {
            if (o.isRequired() && !commandLine.hasOption(o.getName())) {
                usage();
                return;
            }
        }

        String inputFile = commandLine.getOptionValue("i");
        double sparsity = Double.parseDouble(commandLine.getOptionValue("s", "0.5"));
        String outputDirectory = commandLine.getOptionValue("o");
        String workingDirectory = commandLine.getOptionValue("t");
        int n = Integer.parseInt(commandLine.getOptionValue("n"));
        int m = Integer.parseInt(commandLine.getOptionValue("m"));
        int it = Integer.parseInt(commandLine.getOptionValue("it"));
        int k = Integer.parseInt(commandLine.getOptionValue("k"));
        int r = Integer.parseInt(commandLine.getOptionValue("r"));
        String algorithmName = commandLine.getOptionValue("a");

        Path od = new Path(outputDirectory);
        FileSystem odfs = od.getFileSystem(new Configuration());
        if (odfs.exists(od)) {            odfs.delete(od, true);        }
        odfs.mkdirs(od);

        Path wd = new Path(workingDirectory);
        FileSystem wdfs = wd.getFileSystem(new Configuration());
        odfs.mkdirs(new Path(od, "dist"));

        BufferedFileWriter bufferedFileWriter = new BufferedFileWriter();

        if (inputFile == null) {
            bufferedFileWriter.open(wdfs, new Path("input.txt"));
            MatrixInitializer.writeRandomSparseMatrix(n, m, r, sparsity, bufferedFileWriter);
            bufferedFileWriter.close();
            inputFile = "input.txt";
        }


        if (Objects.equals(algorithmName, "NMF")) {

            Algorithm algorithm = new NMF(
                    inputFile, n, m, k, r,
                    workingDirectory, outputDirectory,
                    wd, od, wdfs, odfs
            );

            algorithm.compute(it);

        } else {
            System.out.println("Choose correct algorithm!");
            usage();
        }

    }

}
