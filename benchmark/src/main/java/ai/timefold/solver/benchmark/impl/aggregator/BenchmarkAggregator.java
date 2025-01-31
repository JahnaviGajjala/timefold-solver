package ai.timefold.solver.benchmark.impl.aggregator;
import ai.timefold.solver.benchmark.impl.aggregator.BenchmarkReportGenerator;
import java.io.File;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ai.timefold.solver.benchmark.config.report.BenchmarkReportConfig;
import ai.timefold.solver.benchmark.impl.report.BenchmarkReport;
import ai.timefold.solver.benchmark.impl.report.BenchmarkReportFactory;
import ai.timefold.solver.benchmark.impl.result.PlannerBenchmarkResult;
import ai.timefold.solver.benchmark.impl.result.SingleBenchmarkResult;
import ai.timefold.solver.benchmark.impl.result.SolverBenchmarkResult;
import ai.timefold.solver.benchmark.impl.result.SubSingleBenchmarkResult;
import ai.timefold.solver.benchmark.impl.aggregator.SolverBenchmarkRenamingStrategy;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchmarkAggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkAggregator.class);
        // Add a field for BenchmarkReportGenerator
    private BenchmarkReportGenerator reportGenerator;


    private File benchmarkDirectory = null;
    private BenchmarkReportConfig benchmarkReportConfig = null;

    private SolverBenchmarkRenamingStrategy renamingStrategy;

    public BenchmarkAggregator(SolverBenchmarkRenamingStrategy renamingStrategy) {
        this.renamingStrategy = renamingStrategy;
    }


        // Constructor to initialize BenchmarkReportGenerator
        public BenchmarkAggregator(BenchmarkReportConfig benchmarkReportConfig) {
            this.reportGenerator = new BenchmarkReportGenerator(benchmarkReportConfig);
        }

public BenchmarkAggregator(){

}
    public File getBenchmarkDirectory() {
        return benchmarkDirectory;
    }

    public void setBenchmarkDirectory(File benchmarkDirectory) {
        this.benchmarkDirectory = benchmarkDirectory;
    }

    public BenchmarkReportConfig getBenchmarkReportConfig() {
        return benchmarkReportConfig;
    }

    public void setBenchmarkReportConfig(BenchmarkReportConfig benchmarkReportConfig) {
        this.benchmarkReportConfig = benchmarkReportConfig;
    }

    // ************************************************************************
    // Aggregate methods
    // ************************************************************************

    public File aggregate(List<SingleBenchmarkResult> singleBenchmarkResultList,
            Map<SolverBenchmarkResult, String> solverBenchmarkResultNameMap) {
        if (benchmarkDirectory == null) {
            throw new IllegalArgumentException("The benchmarkDirectory (" + benchmarkDirectory + ") must not be null.");
        }
        if (!benchmarkDirectory.exists()) {
            throw new IllegalArgumentException("The benchmarkDirectory (" + benchmarkDirectory + ") must exist.");
        }
        if (benchmarkReportConfig == null) {
            throw new IllegalArgumentException("The benchmarkReportConfig (" + benchmarkReportConfig
                    + ") must not be null.");
        }
        if (singleBenchmarkResultList.isEmpty()) {
            throw new IllegalArgumentException("The singleBenchmarkResultList (" + singleBenchmarkResultList
                    + ") must not be empty.");
        }
        OffsetDateTime startingTimestamp = OffsetDateTime.now();
        for (SingleBenchmarkResult singleBenchmarkResult : singleBenchmarkResultList) {
            for (SubSingleBenchmarkResult subSingleBenchmarkResult : singleBenchmarkResult.getSubSingleBenchmarkResultList()) {
                subSingleBenchmarkResult.setSingleBenchmarkResult(singleBenchmarkResult);
            }
            singleBenchmarkResult.initSubSingleStatisticMaps();
        }
        // Handle renamed solver benchmarks after statistics have been read (they're resolved by
        // original solver benchmarks' names)
        if (solverBenchmarkResultNameMap != null) {
            for (Entry<SolverBenchmarkResult, String> entry : solverBenchmarkResultNameMap.entrySet()) {
                renamingStrategy.rename(entry.getKey(), entry.getValue());
            }
        }
        PlannerBenchmarkResult plannerBenchmarkResult = PlannerBenchmarkResult.createMergedResult(
                singleBenchmarkResultList);
        plannerBenchmarkResult.setStartingTimestamp(startingTimestamp);
        plannerBenchmarkResult.initBenchmarkReportDirectory(benchmarkDirectory);


        // Existing logic commenting it out...
        // BenchmarkReportFactory benchmarkReportFactory = new BenchmarkReportFactory(benchmarkReportConfig);
        // BenchmarkReport benchmarkReport = benchmarkReportFactory.buildBenchmarkReport(plannerBenchmarkResult);
        // plannerBenchmarkResult.accumulateResults(benchmarkReport);
        // benchmarkReport.writeReport();
        
        // Generate and write the report using BenchmarkReportGenerator
        return reportGenerator.generateReport(plannerBenchmarkResult);

    }

}
