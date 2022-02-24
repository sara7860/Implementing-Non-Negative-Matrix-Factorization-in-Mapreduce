public class Transposer {

    private static class TMapper extends Mapper<LongWritable, Text, LongWritable, Text> {

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            String[] vals = value.toString().split("\\t");
            String[] ai = vals[1].split(",");
            for (int i = 0; i < ai.length; i++) {
                context.write(
                        new LongWritable(i + 1),
                        new Text(vals[0] + "\t" + ai[i])
                );
            }

        }

    }

    private static class TReducer extends Reducer<LongWritable, Text, LongWritable, Text> {

        @Override
        protected void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

            int n = context.getConfiguration().getInt("mh", -1);
            double[] ai = new double[n];

            for (Text value : values) {
                String[] keyVal = value.toString().split("\\t");
                int j = Integer.parseInt(keyVal[0]);
                double aij = Double.parseDouble(keyVal[1]);

                ai[j - 1] = aij;

            }

            StringBuilder result = new StringBuilder();
            for (int j = 0; j < n; j++) {
                result.append(ai[j]);
                if (j < n - 1) {
                    result.append(",");
                }
            }

            context.write(key, new Text(result.toString()));
        }

    }

    private Configuration configuration;
    private String inputPath;
    private String outputPath;

    public Transposer(Configuration configuration, String inputPath, String outputPath) {
        this.configuration = configuration;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }

    public void run() throws IOException, ClassNotFoundException, InterruptedException {

        Job job = Job.getInstance(configuration, "com.lsdp.util.Transposer");

        job.setJarByClass(MRNMF.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);

        job.setMapperClass(TMapper.class);
        job.setReducerClass(TReducer.class);

        job.waitForCompletion(true);
    }

} 
