package ica.fssqawj;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;



public class Solve {
	public static Map< String, ArrayList<Double> > iMap = new HashMap<String, ArrayList<Double> >();
	public static Set<String> iSet = new HashSet<String>();
	public static List<Question> hList = new ArrayList<Question>();
	public static List<Question> rList = new ArrayList<Question>();
	public static Map<String, Integer> hMap = new HashMap<String, Integer>();

	public static Map<String, Integer> rMap = new HashMap<String, Integer>();

	public static Map<Integer, List<Integer>> matchQuestion = new HashMap<Integer, List<Integer>>();

	public static Set<String> jSet = new HashSet<String>();
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File inFile = new File("vecmodel.bin");
		BufferedReader bufferedReader = null;
		
		
		File trainFile = new File("corpus_utf8.txt");
		
		bufferedReader = new BufferedReader(new FileReader(trainFile));
		
		String temp = "";
		
		
		int hcnt = 1;
        int rcnt = 1;
        int cnt = 1;
        int tcnt = 1;
        
        
		while((temp = bufferedReader.readLine()) != null){
			String[] qtem = temp.split("\t####\t");
			List<String> t = getTerm(qtem[0]);
			for(String key : t){
				jSet.add(key);
			}
			t = getTerm(qtem[1]);
			for(String key : t){
				jSet.add(key);
			}
			
			
			String hq = qtem[0];
            String rq = qtem[1];
            //questions[cnt] = new Question();
            if(!hMap.containsKey(hq)){
                hMap.put(hq, hcnt);
                hcnt = hcnt + 1;
                Question tq = new Question();
                tq.setContent(hq);
                hList.add(tq);
            }
            if(!rMap.containsKey(rq)){
                rMap.put(rq, rcnt);
                rcnt = rcnt + 1;
                Question tq = new Question();
                tq.setContent(rq);
                rList.add(tq);
            }

            int hid = hMap.get(hq);
            int rid = rMap.get(rq);

            if(!matchQuestion.containsKey(hid)){
                List<Integer> tem = new ArrayList<Integer>();
                tem.add(rid);
                matchQuestion.put(hid, tem);
            }
            else {
                List<Integer> tem = matchQuestion.get(hid);
                tem.add(rid);
                matchQuestion.put(hid, tem);
            }

            System.out.println(cnt ++);
            if(cnt % 50 == 47){
                iSet.add(hq);
                tcnt = tcnt + 1;
            }
			
		}
		//System.out.println(iSet.size());
		bufferedReader = new BufferedReader(new FileReader(inFile));
		cnt = 0;
		boolean f = false;
		while((temp = bufferedReader.readLine()) != null){
			//System.out.println(temp);
			//if(cnt == 3)break;
			//cnt = cnt + 1;
			if(f){
				String[] tem = temp.split(" ");
				if(jSet.contains(tem[0])){
					ArrayList<Double> t = new ArrayList<Double>();
					for(int i = 1;i < tem.length;i ++){
						//iMap.put(tem[0], )
						t.add(Double.parseDouble(tem[i]));
					}
					iMap.put(tem[0], t);
					//cnt = cnt + 1;
					//System.out.println(cnt);
				}
			}
			f = true;
			
		}
		bufferedReader.close();
		
		
		
		cnt = 0;
        int hit = 0;
        for(int i = 0;i < hList.size();i ++){
            String hq = hList.get(i).getContent();
            if(!iSet.contains(hq))continue;
            System.out.println(cnt ++ + " solved!");
            for(int j = 0;j < rList.size();j ++){
                String rq = rList.get(j).getContent();
                rList.get(j).setSrc(sensVecSim(hq, rq));
            }
            System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
            Collections.sort(rList);
            int hid = hMap.get(hq);
            System.out.println(hq);
            for(int k = 0;k < 10;k ++){
                System.out.println(k+"-------\n" + rList.get(k).getContent());
                System.out.println(rList.get(k).getSrc());
                int id = rMap.get(rList.get(k).getContent());
                if(matchQuestion.get(hid).contains(id)){
                    hit ++;
                    break;
                }
            }


        }
        System.out.println("tcnt : " + tcnt);
        System.out.println("hit : " + hit);
        
        
		
	}
	
	public static List<String> getTerm(String Sentence){
		List<String> iRes = new ArrayList<String>();
		List<Term> iTerm = ToAnalysis.parse(Sentence);
		for(Term key : iTerm){
            String tem = key.toString();
            String[] ary = tem.split("/");
            if(ary.length > 0)iRes.add(ary[0]);
        }
		return iRes;
	}
	
	public static double cos(List<Double> x, List<Double> y){
		double res = 0;
		double resx = 0;
		double resy = 0;
		//System.out.println(x);
		for(int i = 0;i < x.size();i ++){
			res += x.get(i) * y.get(i);
			resx += x.get(i) * x.get(i);
			resy += y.get(i) * y.get(i);
		}
		return res / (Math.sqrt(resx) * Math.sqrt(resy));
	}
	
	public static double sensVecSim(String x, String y){
		List<String> xTerm = getTerm(x);
		List<String> yTerm = getTerm(y);
		double res = 0.0;
		for(String xkey : xTerm){
			if(!iMap.containsKey(xkey)){
				//if(xkey == ykey)
				continue;
			}
			for(String ykey : yTerm){
				if(!iMap.containsKey(ykey)){
					continue;
				}
				double t = cos(iMap.get(xkey), iMap.get(ykey));
				if(t > 0.7)res += t;
			}
		}
		return res / (xTerm.size() * yTerm.size());
	}
}
