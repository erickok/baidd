source("analysis/load.r")
r<-readcsv("exp-metrics/results.csv")
attach(r)

boxplot(e_move ~ PlayOnlyRejects, names=c("arguing","non-arguing"), ylab=expression(f[d]))
means <- c(mean(e_move[PlayOnlyRejects=="false"]),mean(e_move[PlayOnlyRejects=="true"]))
points(means,pch=18,cex=2)

boxplot(e_total.avg_in ~ PlayOnlyRejects, names=c("arguing","non-arguing"), ylab=expression(v[d]))
means <- c(mean(e_total.avg_in[PlayOnlyRejects=="false"]),mean(e_total.avg_in[PlayOnlyRejects=="true"]))
points(means,pch=18,cex=2)

r<-readcsv("exp-baseline/results.csv")
attach(r)

boxplot(e_total.avg_in ~ PlayOnlyRejects, names=c("arguing","baseline"), ylab=expression(v[d]))
means <- c(mean(e_total.avg_in[PlayOnlyRejects=="false"]),mean(e_total.avg_in[PlayOnlyRejects=="true"]))
points(means,pch=18,cex=2)

