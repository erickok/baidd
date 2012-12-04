
error.bars <- function(yv, z, nn, ylabel, xlabel) {
	#xv <- barplot(yv, ylim=c(0, (max(yv) + max(z))), names=nn, ylab=ylabel)
	par(mar=rep(5,4))
	# 1 geeft hier de maximale waarde op de y as
	# cex.lab is text size van de labels
	xv <- barplot(yv, ylim=c(0, (1 + max(z))), axisnames=FALSE, ylab=ylabel, xlab=xlabel, cex.lab=1.5)
	text(xv, par("usr")[3]-0.05, srt=45, adj=c(1.1, 1.1), labels=nn, xpd=TRUE)
	g <- (max(xv) - min(xv)) / 50
	for (i in 1:length(xv)) {
		lines(c(xv[i], xv[i]), c(yv[i] + z[i], yv[i] - z[i]))
		lines(c(xv[i] - g, xv[i] + g), c(yv[i] + z[i], yv[i] + z[i]))
		lines(c(xv[i] - g, xv[i] + g), c(yv[i] - z[i], yv[i] - z[i]))
	}
}

eb <- function(x, y, ylabel, xlabel) {
	labels <- as.character(levels(factor(x)))
	ebl(x, y, labels, ylabel, xlabel)
}

ebl <- function(x, y, xlabels, ylabel, xlabel) {
	means <- as.vector(by(y, x, mean))
	stdev <- as.vector(by(y, x, sd))
	sample <- as.vector(by(y, x, length))
	errors <- stdev/sqrt(sample)
	error.bars(means, errors, xlabels, ylabel, xlabel)
}


readcsv <- function(f) {
	if (file.exists(f)) {
		read.table(f, header=TRUE, sep=";", dec=".", fill=TRUE)
	}
}

#r <- read.table("results.csv", header=TRUE, sep=";", dec=".", fill=TRUE)
r <- readcsv("results.csv")
r1 <- readcsv("experiment1/results.csv")
r2 <- readcsv("experiment2/results.csv")
r3 <- readcsv("experiment3/results.csv")
r4 <- readcsv("experiment4/results.csv")
if (exists("r1") && exists("r2") && exists("r3") && exists("r4")) {
	r <- rbind(r1, r2, r3, r4)
	attach(r)
} else if (exists("r1")) {
	attach(r1)
} else if (exists("r")) {
	attach(r)
}

