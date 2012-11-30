source("~/Dev/baidd/BaiddTest/analysis/load.r")
r<-readcsv("results.csv")
attach(r)

property <- PlayOnlyRejects
xlab1 <- "arguing"
xlab2 <- "non-arguing"

# Move count
boxplot(e_move ~ property, names=c(xlab1,xlab2), ylab="count")
means <- c(mean(e_move[property=="false"]),mean(e_move[property=="true"]))
points(means,pch=18,cex=2)

# Relevance
boxplot(e_relevance.strong ~ property, names=c(xlab1,xlab2), ylab="relevance")
means <- c(mean(e_relevance.strong[property=="false"]),mean(e_relevance.strong[property=="true"]))
points(means,pch=18,cex=2)

# Information concealment
boxplot(e_concealment ~ property, names=c(xlab1,xlab2), ylab="conceal")
means <- c(mean(e_concealment[property=="false"]),mean(e_concealment[property=="true"]))
points(means,pch=18,cex=2)

# Combined utility
boxplot(e_total.o ~ property, names=c(xlab1,xlab2), ylab="utility")
means <- c(mean(e_total.o[property=="false"]),mean(e_total.o[property=="true"]))
points(means,pch=18,cex=2)

# Pareto
plot(e_pareto.o ~ PlayOnlyRejects, ylab="pareto")

# Baseline comparison
boxplot(e_total.avg ~ property, names=c(xlab1,xlab2), ylab="baseline")
means <- c(mean(e_total.avg[property=="false"]),mean(e_total.avg[property=="true"]))
points(means,pch=18,cex=2)

# Baseline comparison
boxplot(e_total.avg, xlab="baseline", ylab="utility")
means <- c(mean(e_total.avg))
points(means,pch=18,cex=2)

