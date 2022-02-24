public class MM1 {

    private static class MM1Mapper extends Mapper<LongWritable, Text, IntWritable, Text> {

        private double[][] B;
        private int Bh;
        private int Bw;
        String prefix;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            String BPath = context.getConfiguration().get("mpath");
            Bw = context.getConfiguration().getInt("mw", -1);
            Bh = context.getConfiguration().getInt("mh", -1);
            prefix = context.getConfiguration().get("prefix", "");

            Path pt = new Path(BPath);
            Configuration conf = new Configuration();
            conf.setBoolean("fs.hdfs.impl.disable.cache", true);
            FileSystem fs = FileSystem.get(conf);

            if (fs.isDirectory(pt)) {
                B = readMatrixFromOutput(pt, Bh, Bw);
            } else {
                B = new double[Bh][Bw];
                readMatrixFromFile(fs, pt, B);
            }

        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            String[] keyVal = value.toString().split("\\t");
            double[] Ai = new double[Bh];
            int i = Integer.parseInt(keyVal[0]) - 1;
            String[] values = keyVal[1].split(",");
            for (int j = 0; j < values.length; j++) {
                Ai[j] = Double.parseDouble(values[j]);
            }
            double[] Ci = new double[Bw];
            StringBuilder result = new StringBuilder(prefix);

            for (int j = 0; j < Bw; j++) {
                Ci[j] = 0d;
                for (int k = 0; k < Bh; k++) {
                    Ci[j] += Ai[k] * B[k][j];
                }
                result.append(Ci[j]);
                if (j != Bw - 1) {
                    result.append(",");
                }
            }
            context.write(new IntWritable(i + 1), new Text(result.toString()));
        }
    }

    private static void readMatrixFromFile(FileSystem fs, Path p, double[][] a) throws IOException {
        FSDataInputStream fsDataInputStream = fs.open(p);
        InputStreamReader inputStreamReader = new InputStreamReader(fsDataInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] keyVal = line.split("\\t");
            int i = Integer.parseInt(keyVal[0]) - 1;
            int j = 0;
            for (String aij : keyVal[1].split(",")) {
                a[i][j++] = Double.parseDouble(aij);
            }
        }
        bufferedReader.close();
        inputStreamReader.close();
        fsDataInputStream.close();
    }

    public static double[][] readMatrixFromOutput(Path dir, int n, int m) throws IOException {

        double[][] a = new double[n][m];

        Configuration conf = new Configuration();
        conf.setBoolean("fs.hdfs.impl.disable.cache", true);
        FileSystem fs = dir.getFileSystem(conf);
        for (Path p : FileUtil.stat2Paths(fs.listStatus(dir))) {
            if (p.toString().contains("part")) {
                readMatrixFromFile(fs, p, a);
            }
        }

        return a;

    }
    
    private Configuration configuration;
    private String inputPath;
    private String outputPath;

    public MM1(Configuration configuration, String inputPath, String outputPath) {
        this.configuration = configuration;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }

    public void run() throws IOException, ClassNotFoundException, InterruptedException {
        
        Job job = Job.getInstance(configuration, "com.lsdp.matrixmultiplication.MM1");

        job.setJarByClass(MRNMF.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Text.class);

        job.setMapperClass(MM1Mapper.class);

        job.waitForCompletion(true);
    }
    }
