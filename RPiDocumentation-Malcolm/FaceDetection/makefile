#
# generic makfile for compiling with OpenCV
# Malcolm Doering
#
#


CFLAGS = `pkg-config --cflags opencv`
LIBS = `pkg-config --libs opencv`

% : %.cpp
	g++ $(CFLAGS) $(LIBS) -o $@ $<


