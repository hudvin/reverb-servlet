package com.nevilon.reverb.servlet;

import edu.washington.cs.knowitall.extractor.ReVerbExtractor;
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunction;
import edu.washington.cs.knowitall.extractor.conf.ReVerbOpenNlpConfFunction;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.OpenNlpSentenceChunker;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import org.json.simple.JSONArray;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/extract")
public class ExtractServlet extends HttpServlet {

    private ReVerbExtractor reverb;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        reverb = new ReVerbExtractor();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req,resp);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        process(req,resp);
    }

    private void process(HttpServletRequest req, HttpServletResponse resp){
        try{
            String sentStr = req.getParameter("text");
            // Looks on the classpath for the default model files.
            OpenNlpSentenceChunker chunker = new OpenNlpSentenceChunker();
            ChunkedSentence sent = chunker.chunkSentence(sentStr);
            ConfidenceFunction confFunc = new ReVerbOpenNlpConfFunction();

            JSONArray obj = new JSONArray();
            for (ChunkedBinaryExtraction extr : reverb.extract(sent)) {
                double conf = confFunc.getConf(extr);
                String arg1 = extr.getArgument1().toString();
                String rel = extr.getRelation().toString();
                String arg2 = extr.getArgument2().toString();
                //fill json
                Map nested = new LinkedHashMap();
                nested.put("arg1", arg1);
                nested.put("rel", rel);
                nested.put("arg2", arg2);
                nested.put("conf", conf);
                obj.add(nested);
            }
            PrintWriter writer = resp.getWriter();
            writer.println(obj.toJSONString());
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }


}